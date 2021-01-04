package main

type UploadRequest struct {
	RequestId string `json:"requestId"`
	Origin    string `json:"origin"`
	Filename  string `json:"filename"`
	LocalPath string `json:"-"` // - = do not show in json
	Key       string `json:"key"`
	Size      int64  `json:"size"`
}

type DownloadRequest struct {
	URL      string `json:"url"`
	Filename string `json:"filename"` // defaults to basename
}

type ListResponse struct {
	Items []ListItem `json:"items"`
}

type ListItem struct {
	Filename string            `json:"filename"`
	Path     string            `json:"path"`
	Tags     map[string]string `json:"tags"`
}
