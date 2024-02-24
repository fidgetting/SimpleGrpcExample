package main

import (
	"addr/address_service"
	"addr/address_service/db"
	"addr/address_service/proto"
	"context"
	"fmt"
	"github.com/jackc/pgx/v5"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"
	"log"
	"net"
)

func main() {
	ctx := context.Background()

	conn, err := pgx.Connect(ctx, "user=anorton password=anorton host=localhost port=5433 dbname=postgres")
	if err != nil {
		log.Fatalf("Unable to connect to db: %v", err)
	}
	defer conn.Close(ctx)
	repository := db.New(conn)

	port := 50052
	lis, err := net.Listen("tcp", fmt.Sprintf("localhost:%d", port))
	if err != nil {
		log.Fatalf("Unable to list on port %v: %v", port, err)
	}

	var opts []grpc.ServerOption
	grpcServer := grpc.NewServer(opts...)
	proto.RegisterAddressServiceServer(grpcServer, &address_service.AddressGrpc{Repository: repository})
	reflection.Register(grpcServer)

	err = grpcServer.Serve(lis)
	if err != nil {
		log.Fatalf("Error starting GRPC server: %v", err)
	}
}
