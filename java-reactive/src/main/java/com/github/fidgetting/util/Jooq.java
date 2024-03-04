package com.github.fidgetting.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.jooq.JSONB;

import static com.github.fidgetting.util.GRPC.exception;
import static io.grpc.Status.INTERNAL;

public class Jooq {

  private static final JsonFormat.Parser parser = JsonFormat.parser().ignoringUnknownFields();
  private static final JsonFormat.Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();

  public static <T extends Message.Builder> T parse(JSONB data, T builder) {
    try {
      parser.merge(data.data(), builder);
      return builder;
    } catch(InvalidProtocolBufferException e) {
      throw exception(INTERNAL, "Unable to parse protobuf: " + data.data(), e);
    }
  }

  public static JSONB toJSONB(Message message) {
    try {
      return JSONB.valueOf(printer.print(message));
    } catch(InvalidProtocolBufferException e) {
      throw exception(INTERNAL, "Unable to serialize protobuf: " + message, e);
    }
  }

}
