package swagger

import (
	"encoding/json"
	"net/http"
)

type ErrorResp struct {
	Msg string `json:"msg"`
}

type AlbumInfoResp struct {
	Artist string `json:"artist"`
	Title  string `json:"title"`
	Year   string `json:"year"`
}

type ImageMetaDataResp struct {
	AlbumID   string `json:"albumID"`
	ImageSize string `json:"imageSize"`
}

func NewAlbum(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	// Mock response, fixed album key, and constant image size.
	resp := ImageMetaDataResp{
		AlbumID:   "fixedAlbumKey123",
		ImageSize: "2048",
	}
	w.WriteHeader(http.StatusOK)
	err := json.NewEncoder(w).Encode(resp)
	if err != nil {
		return
	}
}

func GetAlbumByKey(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	// Mock response, using the example data from the OpenAPI spec.
	resp := AlbumInfoResp{
		Artist: "Sex Pistols",
		Title:  "Never Mind The Bollocks!",
		Year:   "1977",
	}
	w.WriteHeader(http.StatusOK)
	err := json.NewEncoder(w).Encode(resp)
	if err != nil {
		return
	}
}
