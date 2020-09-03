### docs
 
 [docs](https://github.com/typicode/json-server)

### Randomize

```
// index.js
module.exports = () => {
  const data = { users: [] }
  // Create 1000 users
  for (let i = 0; i < 1000; i++) {
    data.users.push({ id: i, name: `user${i}` })
  }
  return data
}
$ json-server index.js
```

### options

```
  --delay, -d        Add delay to responses (ms)
```
json-server --port 8080 --watch db.json --routes routes.json
