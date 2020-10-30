package main
// https://astaxie.gitbooks.io/build-web-application-with-golang/content/en/04.5.html
import (
	"crypto/md5"
	"encoding/json"
	"fmt"
	"github.com/gorilla/mux"
	"io"
	"log"
	"net/http"
	"os"
	"strconv"
	"time"
	 "text/template"
)

const UPLOADIR = "./upload/"

func apiGet(w http.ResponseWriter, r *http.Request) {
	json.NewEncoder(w).Encode(" API endpoint ")
}
// upload logic
func upload(w http.ResponseWriter, r *http.Request) {
	fmt.Println("method:", r.Method)
	if r.Method == "GET" {
		crutime := time.Now().Unix()
		h := md5.New()
		io.WriteString(h, strconv.FormatInt(crutime, 10))
		token := fmt.Sprintf("%x", h.Sum(nil))

		t, _ := template.ParseFiles("upload.gtpl")
		t.Execute(w, token)
	} else {
		vars := mux.Vars(r)
		log.Printf("entityType: %v id %v\n", vars["entityType"],vars["entityId"])
		r.ParseMultipartForm(32 << 20)
		file, handler, err := r.FormFile("uploadfile")
		if err != nil {
			fmt.Println(err)
			return
		}
		defer file.Close()
		fmt.Fprintf(w, "%v", handler.Header)
		f, err := os.OpenFile(UPLOADIR+"/"+handler.Filename, os.O_WRONLY|os.O_CREATE, 0666)
		if err != nil {
			fmt.Println(err)
			return
		}
		defer f.Close()
		io.Copy(f, file)
		log.Printf("Uploaded %s",handler.Filename)
	}
}

func testform(w http.ResponseWriter, r *http.Request) {
	log.Printf("testform")
	w.Write([]byte(`<html>
<head><title>Upload file</title></head>
<body>
<form enctype="multipart/form-data" action="http://127.0.0.1:8090/upload/places/1234" method="post">
    <input type="file" name="uploadfile" />
    <input type="hidden" name="token" value="{{.}}"/>
    <input type="submit" value="upload" />
</form>
</body>
</html>
`))
}
