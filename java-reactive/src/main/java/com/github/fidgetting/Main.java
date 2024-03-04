package com.github.fidgetting;

import com.github.fidgetting.util.GRPC;
import com.github.fidgetting.util.R2DBC;
import io.grpc.protobuf.services.ProtoReflectionService;


public class Main {

  public static void main(String[] args) {
    try (
        var pool = R2DBC.createPool("localhost", 5433, "anorton", "anorton");
        var server = GRPC.server(
            getPort(),
            new AddressService(new AddressRepository(pool.pool)),
            ProtoReflectionService.newInstance()
        )
    ) {
      server.run();
    }
  }

  public static int getPort() {
    var port = System.getenv("ADDRESS_PORT");
    return port == null ? 50051 : Integer.parseInt(port);
  }

}
