# Package for JPA Repositories

The JPA module of Spring Data contains a custom namespace that allows defining repository beans.

## Notebook

* https://stackoverflow.com/questions/8217144/problems-with-making-a-query-when-using-enum-in-entity

```
 @Query(value = "SELECT p FROM Place p where p.lotype = net.timafe.angkor.domain.enums.LocationType.CITY order by p.name")

  @Query("SELECT d FROM Dish d WHERE d.authScope IN (:authScopes)")

  fun findDishesByAuthScope(@Param("authScopes") authScopes: List<AuthScope>): List<Dish>
```

http://www.seanbehan.com/how-to-cast-a-string-of-comma-separated-numbers-into-an-array-of-integers-for-postgres/
```
    
  select * from dish 
  where auth_scope = ANY ('{"ALL_AUTH","PUBLIC"}'::auth_scope[])
```

### Adhoc queries

```
  var query: TypedQuery<Place?>? = em.createQuery("SELECT c FROM Place c where c.lotype=net.timafe.angkor.domain.enums.LocationType.CITY", Place::class.java)
  
  val results: List<Place?> = query!!.getResultList()
```

