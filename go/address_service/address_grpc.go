package address_service

import (
	"addr/address_service/db"
	"addr/address_service/proto"
	"context"
	"errors"
	"github.com/jackc/pgx/v5"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/encoding/protojson"
)

type AddressGrpc struct {
	proto.UnimplementedAddressServiceServer
	Repository *db.Queries
}

func (a *AddressGrpc) GetAddress(ctx context.Context, in *proto.GetAddressRequest) (*proto.GetAddressResponse, error) {
	if in.Id == 0 {
		return nil, status.Errorf(codes.InvalidArgument, "Must provide Address ID in request")
	}

	record, err := a.Repository.GetAddress(ctx, in.Id)
	if errors.Is(err, pgx.ErrNoRows) {
		return nil, status.Errorf(codes.NotFound, "No Address found for %d", in.Id)
	}
	if err != nil {
		return nil, err
	}

	result, err := fromRecord(record)
	if err != nil {
		return nil, err
	}

	return &proto.GetAddressResponse{Address: result}, nil
}

func (a *AddressGrpc) SetAddress(ctx context.Context, in *proto.SetAddressRequest) (*proto.SetAddressResponse, error) {
	json, err := protojson.Marshal(in.Address)
	if err != nil {
		return nil, err
	}

	record := db.Address{}
	if in.Address.Id != 0 {
		record, err = a.Repository.UpsertAddress(ctx, db.UpsertAddressParams{ID: in.Address.Id, Address: json})
	} else {
		record, err = a.Repository.CreateAddress(ctx, json)
	}
	if err != nil {
		return nil, err
	}

	result, err := fromRecord(record)
	if err != nil {
		return nil, err
	}

	return &proto.SetAddressResponse{Address: result}, nil
}

func (a *AddressGrpc) RemoveAddress(ctx context.Context, in *proto.RemoveAddressRequest) (*proto.RemoveAddressResponse, error) {
	if in.Id == 0 {
		return nil, status.Errorf(codes.InvalidArgument, "Must provide Address ID in request")
	}

	record, err := a.Repository.RemoveAddress(ctx, in.Id)
	if err != nil {
		return nil, err
	}

	result, err := fromRecord(record)
	if err != nil {
		return nil, err
	}

	return &proto.RemoveAddressResponse{Address: result}, nil
}

func fromRecord(record db.Address) (*proto.Address, error) {
	result := &proto.Address{}
	err := protojson.Unmarshal(record.Address, result)
	if err != nil {
		return nil, err
	}
	result.Id = record.ID
	return result, nil
}
