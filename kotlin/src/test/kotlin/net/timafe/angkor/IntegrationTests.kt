package net.timafe.angkor

import com.fasterxml.jackson.databind.ObjectMapper
import net.minidev.json.JSONArray
import net.timafe.angkor.config.AppProperties
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Dish
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.enums.*
import net.timafe.angkor.helper.SystemEnvVarActiveProfileResolver
import net.timafe.angkor.helper.TestHelpers
import net.timafe.angkor.helper.TestHelpers.Companion.MOCK_USER
import net.timafe.angkor.repo.*
import net.timafe.angkor.security.SecurityUtils
import net.timafe.angkor.service.AreaService
import net.timafe.angkor.service.EventService
import net.timafe.angkor.service.UserService
import net.timafe.angkor.web.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertNotNull


/**
 * Main Entry Point for most of our Entity related Integration Test
 * 
 * This class is really too fat, we should split the test over multiple classes, e.g. using
 * https://www.baeldung.com/spring-tests#6-using-class-inheritance 
 *
 * Docs for using MockMvc Kotlin DSL:
 * - https://www.baeldung.com/mockmvc-kotlin-dsl
 * - https://github.com/eugenp/tutorials/blob/master/spring-mvc-kotlin/src/test/kotlin/com/baeldung/kotlin/mockmvc/MockMvcControllerTest.kt
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Nice Trick: https://www.allprogrammingtutorials.com/tutorials/overriding-active-profile-boot-integration-tests.php
// Set SPRING_PROFILES_ACTIVE=test to only run test profile (by default, @ActiveProfiles is final)
@ActiveProfiles(
    value = [Constants.PROFILE_TEST, Constants.PROFILE_CLEAN],
    resolver = SystemEnvVarActiveProfileResolver::class
)
@AutoConfigureMockMvc // Enable and configure auto-configuration of MockMvc.
class IntegrationTests(

    // Inject required beans
    // Autowired is still mandatory when used in Test Classes (but no longer in Beans)
    @Autowired val mockMvc: MockMvc,
    @Autowired var objectMapper: ObjectMapper,
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val appProperties: AppProperties,

    // controller  beans to test
    @Autowired val areaController: AreaController,
    @Autowired val dishController: DishController,
    @Autowired val eventController: EventController,
    @Autowired val linkController: LinkController,
    @Autowired val locationController: LocationSearchController,
    @Autowired val metricsController: MetricsController,
    @Autowired val noteController: NoteController,
    @Autowired val placeController: PlaceController,
    @Autowired val postController: PostController,
    @Autowired val tagController: TagController,
    @Autowired val tourController: TourController,
    @Autowired val userController: UserController,
    @Autowired val videoController: VideoController,

    // service beans to test
    @Autowired val areaService: AreaService,
    @Autowired val eventService: EventService,
    @Autowired val userService: UserService,

    // repo beans to test
    @Autowired val eventRepository: EventRepository,
    @Autowired val dishRepository: DishRepository,
    @Autowired val noteRepository: NoteRepository,
    @Autowired val placeRepository: PlaceRepository,
    @Autowired val userRepository: UserRepository,
) {

    //  - attributes -> {Collections.UnmodifiableMap} key value pairs
    //  - iss -> {URL@18497} "https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_...."
    // - sub -> "3913..." (uuid)
    // - cognito:groups -> {JSONArray} with Cognito Group names e.g. eu-central-1blaFacebook, angkor-gurus ...
    // - cognito:roles -> {JSONArray} similar to cognito:groups, but contains role ARNs
    // - cognito:username -> Facebook_16... (for facebook login or the login name for "direct" cognito users)
    // - given_name -> e.g. Gin
    // - family_name -> e.g. Tonic
    // - email -> e.g. gin.tonic@bla.de
    @Test
    fun `test users and roles`() {
        val email = "gin.tonic@monkey.com"
        val firstname = "gin"
        val lastname = "tonic"
        val uuid = "16D2D553-5842-4392-993B-4EA0E7E7C452"
        val roles = JSONArray()
        roles.add("arn:aws:iam::012345678:role/angkor-cognito-role-user")
        roles.add("arn:aws:iam::012345678:role/angkor-cognito-role-admin")
        val attributes = mapOf(
            "groups" to AppRole.USER.withRolePrefix,
            "sub" to uuid,
            SecurityUtils.COGNITO_USERNAME_KEY to "Facebook_hase123",
            SecurityUtils.COGNITO_ROLE_KEY to roles,
            "given_name" to firstname,
            "family_name" to lastname,
            "email" to email,
        )
        // val idToken = OidcIdToken(OidcParameterNames.ID_TOKEN, Instant.now(), Instant.now().plusSeconds(60), attributes)
        // val authorities = listOf(SimpleGrantedAuthority(AppRole.USER.withRolePrefix))
        // val prince = DefaultOidcUser(authorities, idToken)
        val u = userService.findUser(attributes)
        if (u == null) {
            userService.createUser(attributes)
        } else {
            u.lastLogin = ZonedDateTime.now()
            userService.save(u)
        }
        val users = userRepository.findByLoginOrEmailOrId(null, email, null)
        assertThat(users[0].firstName).isEqualTo(firstname)
        assertThat(users[0].lastName).isEqualTo(lastname)
        assertThat(users[0].id).isEqualTo(UUID.fromString(uuid))
        assertThat(users[0].roles).contains("ROLE_USER")
    }

    @Test
    fun testEntityEvents() {
        val randomNum = (0..999).random()
        val eCount = eventRepository.itemCount()
        var someEvent = TestHelpers.someEvent()
        someEvent.message = "I guessed $randomNum"
        someEvent.topic = EventTopic.SYSTEM.topic
        someEvent = eventService.save(someEvent)
        val eventCountAfterSave = eventRepository.findAll().size
        assertThat(eventCountAfterSave).isEqualTo(eCount + 1) // we should have 1 events
        val allEvents = eventController.latestEvents()
        assertThat(allEvents.size).isGreaterThan(0)
        // check if the first element of all latest system events contains our random id
        val latestSystemEvents = eventController.latestEventsByTopic(EventTopic.SYSTEM.topic)
        assertThat(latestSystemEvents[0].message).contains(randomNum.toString())
        eventService.delete(someEvent.id!!)
        assertThat(eventRepository.itemCount()).isEqualTo(eCount) // make sure it's back to initial size
    }

    // Links, Feeds, Videos etc.
    @Test
    fun testFeeds() {
        val items = linkController.getFeeds()
        assertThat(items.size).isGreaterThan(0)
        assertThat(items[0].mediaType).isEqualTo(LinkMediaType.FEED)
        val id = items[0].id
        assertThat(linkController.getFeed(id!!).items.size).isGreaterThan(0)
    }

    // test new generic location table (tours, videos etc.)
    @Test
    fun `test generic public locations`() {
        LocationSearchControllerTest(locationController).testPublic()
    }

    @Test
    @WithMockUser(username = MOCK_USER, roles = ["USER"])
    fun `test generic restricted locations`() {
        LocationSearchControllerTest(locationController).testRestricted()
    }

    @Test
    @WithMockUser(username = MOCK_USER, roles = ["USER"])
    fun `test video CRUD`() {
        VideoControllerTest(videoController).testCRUD()
    }

    @Test
    @WithMockUser(username = MOCK_USER, roles = ["USER"])
    fun `test tour CRUD`() {
        assertThat(appProperties.tours.importFolder).isNotEmpty
        assertThat(appProperties.tourApiBaseUrl).isNotEmpty
        TourControllerTest(tourController).testCRUD()
    }

    @Test
    @WithMockUser(username = MOCK_USER, roles = ["USER"])
    fun `test post CRUD`() {
        PostControllerTest(postController).testCRUD()
    }

    @Test
    @WithMockUser(username = MOCK_USER, roles = ["USER"])
    fun testLinks() {
        val items = linkController.getLinks()
        val origSize = items.size
        assertThat(origSize).isGreaterThan(0)
        var newLink = TestHelpers.someLink()
        newLink = linkController.create(newLink)
        assertThat(linkController.getLinks().size).isEqualTo(origSize + 1)
        newLink.coordinates = arrayListOf(10.0, 20.0)
        linkController.save(newLink, newLink.id!!)
        val findLink = linkController.findOne(newLink.id!!)
        assertThat(findLink.body?.coordinates?.get(0)!!).isEqualTo(10.0)
        linkController.delete(newLink.id!!)
        assertThat(linkController.getLinks().size).isEqualTo(origSize)
        // assertThat(items[0].mediaType).isEqualTo(net.timafe.angkor.domain.enums.LinkMediaType.FEED)
    }

    // Test Entity Controller all searches, all of which should return at least 1 item
    @Test
    @WithMockUser(username = MOCK_USER, roles = ["USER"])
    fun `test searched for none location components`() {
        assertThat(noteController.searchAll().size).isGreaterThan(0)
        //assertThat(placeController.searchAll().size).isGreaterThan(0)
        assertThat(dishController.searchAll().size).isGreaterThan(0)
    }

    // Test Entity Repository all searches, all of which should return at least 1 item
    @Test
    fun `test NativeSQL for none location classes`() {
        val scopes = SecurityUtils.authScopesAsString(listOf(AuthScope.PUBLIC))
        assertThat(dishRepository.search(Pageable.unpaged(), "", scopes).size).isGreaterThan(0)
        assertThat(noteRepository.search(Pageable.unpaged(), "", scopes).size).isGreaterThan(0)
    }

    @Test
    @WithMockUser(username = MOCK_USER, roles = ["USER"])
    fun `test various user controller functions (auth)`() {
        UserControllerTest(userController).testAuthenticated()
        UserControllerTest(userController).`it should throw exception if getAuthentication is not called with subclass of AbstractAuth`()
    }

    // ********************
    // * Area Tests
    // ********************
    @Test
    fun testAreas() {
        assertThat(areaController.areaTree().size).isGreaterThan(0)
        val allAreas = areaController.findAll()
        val totalItems = allAreas.size
        assertThat(totalItems).isGreaterThan(0)
        val area = allAreas[0]
        assertThat(areaController.findOne(area.code)).isNotNull
        area.name = "Hase"
        assertThat(areaController.save(area, area.code).statusCode).isEqualTo(HttpStatus.OK) //
        areaController.delete(area.code)
        assertThat(areaController.findAll().size).isEqualTo(totalItems - 1)
    }

    @Test
    fun `Assert we have areas`() {
        val entity = restTemplate.getForEntity(Constants.API_LATEST + "/areas", String::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body).contains("Thailand")
    }

    @Test
    fun testAreaTree() {
        assertThat(areaService.getAreaTree().size).isGreaterThan(5)
    }

    @Test
    fun testMetrics() {
        MetricsControllerTest(metricsController).`test itemCount stats`()
    }

    @Test
    fun testMetricsAdmin() {
        MetricsControllerTest(metricsController).`test admin status`()
    }

    @Test
    @Throws(Exception::class)
    fun `Assert health`() {
        mockMvc.get("/actuator/health") {
        }.andExpect {
            status { isOk() }
            content { contentType("application/vnd.spring-boot.actuator.v3+json") }
            jsonPath("$.status") { value("UP") }
        }
    }


    // ***********
    // Dish Tests
    // ***********
    @Test
    fun testAllDishes() {
        val dishes = dishRepository.findAll().toList()
        assertThat(dishes.size).isGreaterThan(1)
        dishes[0].name = dishes[0].name.reversed()
        dishController.save(dishes[0], dishes[0].id)
    }

    @Test
    fun testEventsAccessible() {
        // todo test real data, for now test at least if query works
        assertThat(eventRepository.findAll().size).isGreaterThan(-1)
    }


    @Test
    @Throws(Exception::class)
    // We can also easily customize the roles. For example, this test will be invoked with the username "hase" and the roles "ROLE_USER"
    @WithMockUser(username = MOCK_USER, roles = ["USER"])
    fun `it should create a new place`() {

        val mvcResult = mockMvc.post(Constants.API_LATEST + "/places") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(TestHelpers.somePlace())
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { /*isOk()*/ isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { string(containsString("hase")) }
            jsonPath("$.name") { value("hase") }
            jsonPath("$.summary") { value("nice place") }
            /*content { json("{}") }*/
        }.andDo {
            /* print ())*/
        }.andReturn()

        val newPlace = objectMapper.readValue(mvcResult.response.contentAsString, Place::class.java)
        assertThat(newPlace.id).isNotNull
        // objectMapper.writeValue(System.out,newPlace)
    }

    @Test
    @Throws(Exception::class)
    @WithMockUser(username = MOCK_USER, roles = ["USER"])
    fun `it should update an existing place`() {
        val id = UUID.randomUUID()
        val existPlace = TestHelpers.somePlace()
        existPlace.id = id // make sure we use a new id
        placeController.create(existPlace) // create first, update later
        existPlace.name = "newName"
        val mvcResult = mockMvc.put(Constants.API_LATEST + "/places/${id}") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(existPlace)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { /*isOk()*/ isOk() } // update returns 200, not 201 (created)
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.name") { value("newName") }
            jsonPath("$.summary") { value("nice place") }
            /*content { json("{}") }*/
        }.andDo {
            /* print ())*/
        }.andReturn()

        val newPlace = objectMapper.readValue(mvcResult.response.contentAsString, Place::class.java)
        assertThat(newPlace.id).isEqualTo(existPlace.id)
    }

    @Test
    @WithMockUser(username = MOCK_USER, roles = ["USER"])
    fun `it should create a yummy new dish`() {
        val mvcResult = mockMvc.post(Constants.API_LATEST + "/dishes") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(TestHelpers.someDish())
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { /*isOk()*/ isCreated() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.name") { value("some food") }
        }.andReturn()

        val newDish = objectMapper.readValue(mvcResult.response.contentAsString, Dish::class.java)
        assertThat(newDish.id).isNotNull
    }

    @Test
    @WithMockUser(username = MOCK_USER, roles = ["USER"])
    fun `it should update an existing dish (put)`() {
        val existDish = TestHelpers.someDish()
        val savedDish = dishController.create(existDish) // create first, update later
        savedDish.name = "moreSpicy"
        val mvcResult = mockMvc.put(Constants.API_LATEST + "/dishes/${savedDish.id}") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(savedDish)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { /*isOk()*/ isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.name") { value("moreSpicy") }
        }.andReturn()

        val newDish = objectMapper.readValue(mvcResult.response.contentAsString, Dish::class.java)
        assertThat(newDish.id).isEqualTo(savedDish.id)
    }

    @Test
    fun testGetDishes() {
        mockMvc.get(Constants.API_LATEST + "/dishes/search/") {
        }.andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
        }
    }

    @Test
    @Throws(Exception::class)
    @WithMockUser(username = MOCK_USER, roles = ["USER"])
    fun `Assert dish count is incremented`() {
        val dish = dishController.searchAll()[0]
        val origCount = dishController.findOne(dish.id).body!!.timesServed
        assertNotNull(dish)
        mockMvc.put(Constants.API_LATEST + "/dishes/${dish.id}/just-served") {
        }.andExpect {
            status { isOk() }
            // {"result":1}
            // content { string(containsString("hase")) }
            jsonPath("$.result") { value(origCount + 1) }
        }
    }

    @Test
    @Throws(Exception::class)
    @WithMockUser(username = MOCK_USER, roles = ["USER"])
    fun testUserSummaries() {
        mockMvc.get(Constants.API_LATEST + "/user-summaries") {
        }.andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
            // son path value method can take org.hamcrest.Matcher as parameter.
            // So you can use GreaterThan class: jsonPath("['key']").value(new GreaterThan(1))
            jsonPath("$.length()") { value(org.hamcrest.Matchers.greaterThan(0)) } // returns only hase
        } /*.andDo { print() } */
    }

    // ************
    // Note tests
    // ************
    @Test
    @WithMockUser(username = MOCK_USER, roles = ["USER"])
    fun `Assert Place gets created form Note and status is set to Closed`() {
        val note = noteController.create(TestHelpers.someNote())
        assertThat(note.id).isNotNull
        note.status = NoteStatus.OPEN
        val place = noteController.createPlaceFromNote(note)
        assertThat(place.name).isEqualTo(note.summary)
        assertThat(place.primaryUrl).isEqualTo(note.primaryUrl)
        assertThat(place.authScope).isEqualTo(note.authScope)
        assertThat(place.id).isNotNull
        val savedNote = noteController.findOne(note.id) // asserted above to be true
        assertThat(savedNote.body!!.status).isEqualTo(NoteStatus.CLOSED)
    }

    @Test
    @Throws(Exception::class)
    fun `Assert we get notes`() {
        mockMvc.get(Constants.API_LATEST + "/notes/search/") {
        }.andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
        }
    }

    // ***********
    // Misc Tests
    // ***********
    @Test
    @WithMockUser(username = MOCK_USER, roles = ["USER"])
    fun `Assert authentication`() {
        mockMvc.get("${Constants.API_LATEST}/authenticated") {
        }.andExpect {
            status { isOk() }
            jsonPath("$.result") { value(true) }
        }
    }

    @Test
    fun testAllTags() {
        assertThat(tagController.allTags().size).isGreaterThan(2)
    }

    // MockMvc Kotlin DSL
    // https://www.baeldung.com/mockmvc-kotlin-dsl
    // https://www.petrikainulainen.net/programming/spring-framework/integration-testing-of-spring-mvc-applications-write-clean-assertions-with-jsonpath/
    @Test
    fun `it should return places location pois`() {
        mockMvc.get(Constants.API_LATEST + "/pois/places") {
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$") { isArray() }
            jsonPath("$.length()") { value(org.hamcrest.Matchers.greaterThan(0)) } // returns only hase
            jsonPath("$[0].entityType") { value("Place") }
        }.andDo { /* print() */ }.andReturn()
    }

    @Test
    fun `it should return tours location pois`() {
        mockMvc.get(Constants.API_LATEST + "/pois/tours") {
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$") { isArray() }
            jsonPath("$.length()") { value(org.hamcrest.Matchers.greaterThan(0)) }
            jsonPath("$[0].entityType") { value("Tour") }
        }.andDo {  /* print() */ }.andReturn() // in case you need the MvcResult
    }
}
