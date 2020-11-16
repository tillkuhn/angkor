package main

const imageContentType = "image/jpeg"

type UploadRequest struct {
	LocalPath string `json:"-"`
	Key       string `json:"key"`
	RequestId string `json:"requestId"`
	Size      int64 `json:"size"`
	// Delay time.Duration
}

type ListResponse struct {
	Items []ListItem `json:"items"`
}

type ListItem struct {
	Filename string `json:"filename"`
	//PresignedUrl string            `json:"presignedUrl"`
	Path string            `json:"path"`
	Tags map[string]string `json:"tags"`
}
