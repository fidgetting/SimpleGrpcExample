package main

//go:generate protoc --go_out=./address_service --proto_path=./address_service/proto address_service/proto/address.proto
//go:generate protoc --go_out=./address_service --proto_path=./address_service/proto --go-grpc_out=./address_service address_service/proto/address_service.proto
//go:generate sqlc generate
