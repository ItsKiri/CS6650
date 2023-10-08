module main.go

go 1.21.1

replace example.com/routers => ./go/

require example.com/routers v0.0.0-00010101000000-000000000000

require github.com/gorilla/mux v1.8.0 // indirect
