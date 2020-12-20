scratch: # run typescript scratchfile
	ts-node scratch.ts

scra: scratch

run: 
	yarn open

test:
	yarn test:headless

mock: 
	json-server --port 8080 --watch --routes json-server/routes.json json-server/db.json

check-update:
	ng update