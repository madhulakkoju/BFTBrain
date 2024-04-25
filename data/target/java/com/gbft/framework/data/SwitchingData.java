// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: gbft.proto

package com.gbft.framework.data;

/**
 * Protobuf type {@code SwitchingData}
 */
public final class SwitchingData extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:SwitchingData)
    SwitchingDataOrBuilder {
private static final long serialVersionUID = 0L;
  // Use SwitchingData.newBuilder() to construct.
  private SwitchingData(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private SwitchingData() {
    nextProtocol_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new SwitchingData();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.gbft.framework.data.Gbft.internal_static_SwitchingData_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.gbft.framework.data.Gbft.internal_static_SwitchingData_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.gbft.framework.data.SwitchingData.class, com.gbft.framework.data.SwitchingData.Builder.class);
  }

  public static final int SWITCH_READY_FIELD_NUMBER = 1;
  private boolean switchReady_ = false;
  /**
   * <code>bool switch_ready = 1;</code>
   * @return The switchReady.
   */
  @java.lang.Override
  public boolean getSwitchReady() {
    return switchReady_;
  }

  public static final int NEXT_PROTOCOL_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private volatile java.lang.Object nextProtocol_ = "";
  /**
   * <code>string next_protocol = 2;</code>
   * @return The nextProtocol.
   */
  @java.lang.Override
  public java.lang.String getNextProtocol() {
    java.lang.Object ref = nextProtocol_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      nextProtocol_ = s;
      return s;
    }
  }
  /**
   * <code>string next_protocol = 2;</code>
   * @return The bytes for nextProtocol.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getNextProtocolBytes() {
    java.lang.Object ref = nextProtocol_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      nextProtocol_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (switchReady_ != false) {
      output.writeBool(1, switchReady_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(nextProtocol_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, nextProtocol_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (switchReady_ != false) {
      size += com.google.protobuf.CodedOutputStream
        .computeBoolSize(1, switchReady_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(nextProtocol_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, nextProtocol_);
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof com.gbft.framework.data.SwitchingData)) {
      return super.equals(obj);
    }
    com.gbft.framework.data.SwitchingData other = (com.gbft.framework.data.SwitchingData) obj;

    if (getSwitchReady()
        != other.getSwitchReady()) return false;
    if (!getNextProtocol()
        .equals(other.getNextProtocol())) return false;
    if (!getUnknownFields().equals(other.getUnknownFields())) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + SWITCH_READY_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
        getSwitchReady());
    hash = (37 * hash) + NEXT_PROTOCOL_FIELD_NUMBER;
    hash = (53 * hash) + getNextProtocol().hashCode();
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.gbft.framework.data.SwitchingData parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.gbft.framework.data.SwitchingData parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.gbft.framework.data.SwitchingData parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.gbft.framework.data.SwitchingData parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.gbft.framework.data.SwitchingData parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.gbft.framework.data.SwitchingData parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.gbft.framework.data.SwitchingData parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.gbft.framework.data.SwitchingData parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.gbft.framework.data.SwitchingData parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.gbft.framework.data.SwitchingData parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.gbft.framework.data.SwitchingData parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.gbft.framework.data.SwitchingData parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(com.gbft.framework.data.SwitchingData prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code SwitchingData}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:SwitchingData)
      com.gbft.framework.data.SwitchingDataOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.gbft.framework.data.Gbft.internal_static_SwitchingData_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.gbft.framework.data.Gbft.internal_static_SwitchingData_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.gbft.framework.data.SwitchingData.class, com.gbft.framework.data.SwitchingData.Builder.class);
    }

    // Construct using com.gbft.framework.data.SwitchingData.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      switchReady_ = false;
      nextProtocol_ = "";
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.gbft.framework.data.Gbft.internal_static_SwitchingData_descriptor;
    }

    @java.lang.Override
    public com.gbft.framework.data.SwitchingData getDefaultInstanceForType() {
      return com.gbft.framework.data.SwitchingData.getDefaultInstance();
    }

    @java.lang.Override
    public com.gbft.framework.data.SwitchingData build() {
      com.gbft.framework.data.SwitchingData result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.gbft.framework.data.SwitchingData buildPartial() {
      com.gbft.framework.data.SwitchingData result = new com.gbft.framework.data.SwitchingData(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.gbft.framework.data.SwitchingData result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.switchReady_ = switchReady_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.nextProtocol_ = nextProtocol_;
      }
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof com.gbft.framework.data.SwitchingData) {
        return mergeFrom((com.gbft.framework.data.SwitchingData)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.gbft.framework.data.SwitchingData other) {
      if (other == com.gbft.framework.data.SwitchingData.getDefaultInstance()) return this;
      if (other.getSwitchReady() != false) {
        setSwitchReady(other.getSwitchReady());
      }
      if (!other.getNextProtocol().isEmpty()) {
        nextProtocol_ = other.nextProtocol_;
        bitField0_ |= 0x00000002;
        onChanged();
      }
      this.mergeUnknownFields(other.getUnknownFields());
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 8: {
              switchReady_ = input.readBool();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
            case 18: {
              nextProtocol_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            default: {
              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                done = true; // was an endgroup tag
              }
              break;
            } // default:
          } // switch (tag)
        } // while (!done)
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.unwrapIOException();
      } finally {
        onChanged();
      } // finally
      return this;
    }
    private int bitField0_;

    private boolean switchReady_ ;
    /**
     * <code>bool switch_ready = 1;</code>
     * @return The switchReady.
     */
    @java.lang.Override
    public boolean getSwitchReady() {
      return switchReady_;
    }
    /**
     * <code>bool switch_ready = 1;</code>
     * @param value The switchReady to set.
     * @return This builder for chaining.
     */
    public Builder setSwitchReady(boolean value) {
      
      switchReady_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>bool switch_ready = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearSwitchReady() {
      bitField0_ = (bitField0_ & ~0x00000001);
      switchReady_ = false;
      onChanged();
      return this;
    }

    private java.lang.Object nextProtocol_ = "";
    /**
     * <code>string next_protocol = 2;</code>
     * @return The nextProtocol.
     */
    public java.lang.String getNextProtocol() {
      java.lang.Object ref = nextProtocol_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        nextProtocol_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string next_protocol = 2;</code>
     * @return The bytes for nextProtocol.
     */
    public com.google.protobuf.ByteString
        getNextProtocolBytes() {
      java.lang.Object ref = nextProtocol_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        nextProtocol_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string next_protocol = 2;</code>
     * @param value The nextProtocol to set.
     * @return This builder for chaining.
     */
    public Builder setNextProtocol(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      nextProtocol_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>string next_protocol = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearNextProtocol() {
      nextProtocol_ = getDefaultInstance().getNextProtocol();
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }
    /**
     * <code>string next_protocol = 2;</code>
     * @param value The bytes for nextProtocol to set.
     * @return This builder for chaining.
     */
    public Builder setNextProtocolBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      nextProtocol_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:SwitchingData)
  }

  // @@protoc_insertion_point(class_scope:SwitchingData)
  private static final com.gbft.framework.data.SwitchingData DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.gbft.framework.data.SwitchingData();
  }

  public static com.gbft.framework.data.SwitchingData getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SwitchingData>
      PARSER = new com.google.protobuf.AbstractParser<SwitchingData>() {
    @java.lang.Override
    public SwitchingData parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      Builder builder = newBuilder();
      try {
        builder.mergeFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(builder.buildPartial());
      } catch (com.google.protobuf.UninitializedMessageException e) {
        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(e)
            .setUnfinishedMessage(builder.buildPartial());
      }
      return builder.buildPartial();
    }
  };

  public static com.google.protobuf.Parser<SwitchingData> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SwitchingData> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.gbft.framework.data.SwitchingData getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

