package net.timafe.angkor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.minidev.json.JSONArray
import net.timafe.angkor.config.Constants
import net.timafe.angkor.domain.Place
import net.timafe.angkor.domain.enums.AppRole
import net.timafe.angkor.domain.enums.AuthScope
import net.timafe.angkor.helper.SytemEnvVarActiveProfileResolver
import net.timafe.angkor.helper.TestHelpers
import net.timafe.angkor.repo.*
import net.timafe.angkor.web.*
import net.timafe.angkor.security.SecurityUtils
import net.timafe.angkor.service.AreaService
import net.timafe.angkor.service.UserService
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
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertNotNull

/**
 * https://www.baeldung.com/mockmvc-kotlin-dsl
 * https://github.com/eugenp/tutorials/blob/master/spring-mvc-kotlin/src/test/kotlin/com/baeldung/kotlin/mockmvc/MockMvcControllerTest.kt
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Nice Trick: https://www.allprogrammingtutorials.com/tutorials/overriding-active-profile-boot-integration-tests.php
// Set SPRING_PROFILES_ACTIVE=test to only run test profile (by default, @ActiveProfiles is final)
@ActiveProfiles(value = [Constants.PROFILE_TEST, Constants.PROFILE_CLEAN],resolver = SytemEnvVarActiveProfileResolver::class )
@AutoConfigureMockMvc
class IntegrationTests(
    // Autowired is mandatory here
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val eventRepository: EventRepository,
    @Autowired val mockMvc: MockMvc,
    @Autowired var objectMapper: ObjectMapper,

    // controller  beans to test
    @Autowired val tagController: TagController,
    @Autowired val placeController: PlaceController,
    @Autowired val noteController: NoteController,
    @Autowired val dishController: DishController,
    @Autowired val metricsController: MetricsController,
    @Autowired val areaController: AreaController,
    @Autowired val linkController: LinkController,

    // controller  beans to test
    @Autowired val areaService: AreaService,
    @Autowired val userService: UserService,

    // repo beans to test
    @Autowired val userRepository: UserRepository,
    @Autowired val dishRepository: DishRepository,
    @Autowired val noteRepository: NoteRepository,
    @Autowired val placeRepository: PlaceRepository
) {

//    *   - attributes -> {Collections.UnmodifiableMap} key value pairs
//    *     - iss -> {URL@18497} "https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_...."
//    *     - sub -> "3913..." (uuid)
//    *     - cognito:groups -> {JSONArray} with Cognito Group names e.g. eu-central-1blaFacebook, angkor-gurus etc-
//    *     - cognito:roles -> {JSONArray} similar to cognito:groups, but contains role arns
//    *     - cognito:username -> Facebook_16... (for facebook login or the loginname for "direct" cognito users)
//    *     - given_name -> e.g. Gin
//    *     - family_name -> e.g. Tonic
//    *     - email -> e.g. gin.tonic@bla.de
    @Test
    fun testUsers() {
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
        //  val idToken = OidcIdToken(OidcParameterNames.ID_TOKEN, Instant.now(), Instant.now().plusSeconds(60), attributes)
        // val authorities = listOf(SimpleGrantedAuthority(AppRole.USER.withRolePrefix))
        // val prince = DefaultOidcUser(authorities, idToken)
        val u = userService.findUser(attributes)
        if (u == null) {
            userService.createUser(attributes)
        } else {
            u.lastLogin = LocalDateTime.now()
            userService.save(u)
        }
        val users = userRepository.findByLoginOrEmailOrId(null,email,null)
        assertThat(users[0].firstName).isEqualTo(firstname)
        assertThat(users[0].lastName).isEqualTo(lastname)
        assertThat(users[0].id).isEqualTo(UUID.fromString(uuid))
        assertThat(users[0].roles).contains("ROLE_USER")
    }

    @Test
    fun testEntityEvents() {
        val differentRepos = 3
        val eventCount = eventRepository.findAll().size
        val place = placeController.create(TestHelpers.somePlace())
        val dish = dishController.create(TestHelpers.someDish())
        val note = noteController.create(TestHelpers.someNote())
        assertThat(place).isNotNull
        assertThat(dish).isNotNull
        assertThat(note).isNotNull
        val eventCountAfterAdd = eventRepository.findAll().size
        assertThat(eventCountAfterAdd).isEqualTo(eventCount+differentRepos) // we should have 3 new entity created events
        placeController.delete(place.id!!)
        dishController.delete(dish.id!!)
        noteController.delete(note.id!!)
        val eventCountAfterRemove = eventRepository.findAll().size
        assertThat(eventCountAfterRemove).isEqualTo(eventCountAfterAdd+differentRepos) // we should have 3 new entity delete events
    }

    // Links, Feeds, Videos etc.
    @Test
    fun testFeeds() {
        val items = linkController.getFeeds()
        assertThat(items.size).isGreaterThan(0)
        assertThat(items[0].mediaType).isEqualTo(net.timafe.angkor.domain.enums.LinkMediaType.FEED)
    }

    @Test
    fun testVideos() {
        val videos = linkController.getVideos()
        assertThat(videos.size).isGreaterThan(0)
        assertThat(videos[0].mediaType).isEqualTo(net.timafe.angkor.domain.enums.LinkMediaType.VIDEO)
    }

    @Test
    fun testLinks() {
        val items = linkController.getLinks()
        val origSize = items.size
        assertThat(origSize).isGreaterThan(0)
        var newLink = TestHelpers.someLink()
        newLink = linkController.create(newLink)
        assertThat(linkController.getLinks().size).isEqualTo(origSize+1)
        linkController.delete(newLink.id!!)
        assertThat(linkController.getLinks().size).isEqualTo(origSize)
        // assertThat(items[0].mediaType).isEqualTo(net.timafe.angkor.domain.enums.LinkMediaType.FEED)
    }


    @Test
    @WithMockUser(username = "hase", roles = ["USER"])
    fun testSearches() {
        assertThat(noteController.searchAll().size).isGreaterThan(0)
        assertThat(placeController.searchAll().size).isGreaterThan(0)
        assertThat(dishController.searchAll().size).isGreaterThan(0)
    }

    @Test
    fun testAreas() {
        assertThat(areaController.areaTree().size).isGreaterThan(0)
    }

    @Test
    fun testMetrics() {
        val stats = metricsController.entityStats()
        assertThat(stats["places"]).isGreaterThan(0)
        assertThat(stats["notes"]).isGreaterThan(0)
        assertThat(stats["pois"]).isGreaterThan(0)
        assertThat(stats["dishes"]).isGreaterThan(0)
    }

    @Test
    fun testMetricsAdmin() {
        val stats = metricsController.metrics()
        assertThat(stats.size).isGreaterThan(15)
    }

    @Test
    fun testAreaTree() {
        assertThat(areaService.getAreaTree().size).isGreaterThan(5)
    }

    @Test
    fun testAllTags() {
        assertThat(tagController.alltags().size).isGreaterThan(2)
    }

    @Test
    fun testAllDishes() {
        val dishes = dishRepository.findAll().toList()
        assertThat(dishes.size).isGreaterThan(1)
        dishes[0].name=dishes[0].name.reversed()
        dishController.save( dishes[0], dishes[0].id!!)
    }

    @Test
    fun testEventsAccessible() {
        // todo test real data, for now test at least if query works
        assertThat(eventRepository.findAll().size).isGreaterThan(-1)
    }

    @Test
    fun testNativeSQL() {
        val scopes = SecurityUtils.authScopesAsString(listOf(AuthScope.PUBLIC))
        assertThat(dishRepository.search(Pageable.unpaged(), "", scopes).size).isGreaterThan(0)
        assertThat(noteRepository.search(Pageable.unpaged(), "", scopes).size).isGreaterThan(0)
        assertThat(placeRepository.search(Pageable.unpaged(), "", scopes).size).isGreaterThan(0)
    }

//    @Test
//    @Throws(Exception::class)
//    @WithMockUser(username = "hase", roles = ["USER"])
//    fun testFileUpload() {
//        val firstFile = MockMultipartFile("file", "recipe.txt", "text/plain", "pasta".toByteArray())
//        mockMvc.perform(
//            MockMvcRequestBuilders.multipart("${Constants.API_LATEST}/${Constants.API_PATH_PLACES}/815/${Constants.API_PATH_FILES}")
//                .file(firstFile)
//                .param("some-random", "4")
//        )
//            .andExpect(MockMvcResultMatchers.status().`is`(200))
//            .andExpect(MockMvcResultMatchers.content().string(containsString("Successfully")))
//    }


    @Test
    @Throws(Exception::class)
    // We can also easily customize the roles. For example, this test will be invoked with the username "hase" and the roles "ROLE_USER"
    @WithMockUser(username = "hase", roles = ["USER"])
    fun testPlacePost() {

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
    fun testGetDishes() {
        mockMvc.get(Constants.API_LATEST + "/dishes/search/") {
        }.andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
        }.andDo { print() }
    }

    @Test
    @Throws(Exception::class)
    @WithMockUser(username = "hase", roles = ["USER"])
    fun `Assert dish count is incremented`() {
        val dish = dishController.searchAll()[0]
        val origCount = dishController.findOne(dish.id).body!!.timesServed
        assertNotNull(dish)
        mockMvc.put( Constants.API_LATEST + "/dishes/${dish.id}/just-served") {
        }.andExpect {
            status { isOk() }
            // {"result":1}
            // content { string(containsString("hase")) }
            jsonPath("$.result") { value(origCount+1)}
        }
    }

    @Test
    @Throws(Exception::class)
    @WithMockUser(username = "hase", roles = ["USER"])
    fun testUserSummaries() {
        mockMvc.get(Constants.API_LATEST + "/user-summaries") {
        }.andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
            // son path value method can take org.hamcrest.Matcher as parameter.
            // So you can use GreaterThan class: jsonPath("['key']").value(new GreaterThan(1))
            jsonPath("$.length()") { value(org.hamcrest.Matchers.greaterThan(0)) } // returns only hase
            // org.hamcrest.Matchers.greaterThan(T value)
            //  jsonPath("$.length()") {org.hamcrest.Matchers.greaterThan(2) }
        }.andDo { print() }
    }

    @Test
    @Throws(Exception::class)
    // https://www.baeldung.com/mockmvc-kotlin-dsl
    fun testGetPois() {
        objectMapper.registerKotlinModule()
        /*val mvcResult = */ mockMvc.get(Constants.API_LATEST + "/pois") {
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$") { isArray() }
            jsonPath("$.length()") { value(6) }
            // .andExpect(jsonPath("$.description", is("Lorem ipsum")))
        }.andDo { /* print() */ }.andReturn()
        // val actual: List<POI?>? = objectMapper.readValue(mvcResult.response.contentAsString, object : TypeReference<List<POI?>?>() {})
        // assertThat(actual?.size).isGreaterThan(0)
    }

    @Test
    @Throws(Exception::class)
    fun `Assert health`() {
        mockMvc.get( "/actuator/health") {
        }.andExpect {
            status { isOk() }
            content { contentType("application/vnd.spring-boot.actuator.v3+json") }
            jsonPath("$.status") {value("UP") }
        }
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

    @Test
    fun `Assert we have areas`() {
        val entity = restTemplate.getForEntity(Constants.API_LATEST + "/areas", String::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body).contains("Thailand")
    }

}
