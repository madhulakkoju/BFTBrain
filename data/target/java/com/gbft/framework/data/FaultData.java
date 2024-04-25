// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: gbft.proto

package com.gbft.framework.data;

/**
 * Protobuf type {@code FaultData}
 */
public final class FaultData extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:FaultData)
    FaultDataOrBuilder {
private static final long serialVersionUID = 0L;
  // Use FaultData.newBuilder() to construct.
  private FaultData(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private FaultData() {
    blockedTargets_ = emptyIntList();
    delayedTargets_ = emptyIntList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new FaultData();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.gbft.framework.data.Gbft.internal_static_FaultData_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.gbft.framework.data.Gbft.internal_static_FaultData_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.gbft.framework.data.FaultData.class, com.gbft.framework.data.FaultData.Builder.class);
  }

  public static final int BLOCKED_TARGETS_FIELD_NUMBER = 1;
  @SuppressWarnings("serial")
  private com.google.protobuf.Internal.IntList blockedTargets_;
  /**
   * <code>repeated int32 blocked_targets = 1;</code>
   * @return A list containing the blockedTargets.
   */
  @java.lang.Override
  public java.util.List<java.lang.Integer>
      getBlockedTargetsList() {
    return blockedTargets_;
  }
  /**
   * <code>repeated int32 blocked_targets = 1;</code>
   * @return The count of blockedTargets.
   */
  public int getBlockedTargetsCount() {
    return blockedTargets_.size();
  }
  /**
   * <code>repeated int32 blocked_targets = 1;</code>
   * @param index The index of the element to return.
   * @return The blockedTargets at the given index.
   */
  public int getBlockedTargets(int index) {
    return blockedTargets_.getInt(index);
  }
  private int blockedTargetsMemoizedSerializedSize = -1;

  public static final int DELAYED_TARGETS_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private com.google.protobuf.Internal.IntList delayedTargets_;
  /**
   * <code>repeated int32 delayed_targets = 2;</code>
   * @return A list containing the delayedTargets.
   */
  @java.lang.Override
  public java.util.List<java.lang.Integer>
      getDelayedTargetsList() {
    return delayedTargets_;
  }
  /**
   * <code>repeated int32 delayed_targets = 2;</code>
   * @return The count of delayedTargets.
   */
  public int getDelayedTargetsCount() {
    return delayedTargets_.size();
  }
  /**
   * <code>repeated int32 delayed_targets = 2;</code>
   * @param index The index of the element to return.
   * @return The delayedTargets at the given index.
   */
  public int getDelayedTargets(int index) {
    return delayedTargets_.getInt(index);
  }
  private int delayedTargetsMemoizedSerializedSize = -1;

  public static final int DELAY_FIELD_NUMBER = 3;
  private long delay_ = 0L;
  /**
   * <code>int64 delay = 3;</code>
   * @return The delay.
   */
  @java.lang.Override
  public long getDelay() {
    return delay_;
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
    getSerializedSize();
    if (getBlockedTargetsList().size() > 0) {
      output.writeUInt32NoTag(10);
      output.writeUInt32NoTag(blockedTargetsMemoizedSerializedSize);
    }
    for (int i = 0; i < blockedTargets_.size(); i++) {
      output.writeInt32NoTag(blockedTargets_.getInt(i));
    }
    if (getDelayedTargetsList().size() > 0) {
      output.writeUInt32NoTag(18);
      output.writeUInt32NoTag(delayedTargetsMemoizedSerializedSize);
    }
    for (int i = 0; i < delayedTargets_.size(); i++) {
      output.writeInt32NoTag(delayedTargets_.getInt(i));
    }
    if (delay_ != 0L) {
      output.writeInt64(3, delay_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    {
      int dataSize = 0;
      for (int i = 0; i < blockedTargets_.size(); i++) {
        dataSize += com.google.protobuf.CodedOutputStream
          .computeInt32SizeNoTag(blockedTargets_.getInt(i));
      }
      size += dataSize;
      if (!getBlockedTargetsList().isEmpty()) {
        size += 1;
        size += com.google.protobuf.CodedOutputStream
            .computeInt32SizeNoTag(dataSize);
      }
      blockedTargetsMemoizedSerializedSize = dataSize;
    }
    {
      int dataSize = 0;
      for (int i = 0; i < delayedTargets_.size(); i++) {
        dataSize += com.google.protobuf.CodedOutputStream
          .computeInt32SizeNoTag(delayedTargets_.getInt(i));
      }
      size += dataSize;
      if (!getDelayedTargetsList().isEmpty()) {
        size += 1;
        size += com.google.protobuf.CodedOutputStream
            .computeInt32SizeNoTag(dataSize);
      }
      delayedTargetsMemoizedSerializedSize = dataSize;
    }
    if (delay_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(3, delay_);
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
    if (!(obj instanceof com.gbft.framework.data.FaultData)) {
      return super.equals(obj);
    }
    com.gbft.framework.data.FaultData other = (com.gbft.framework.data.FaultData) obj;

    if (!getBlockedTargetsList()
        .equals(other.getBlockedTargetsList())) return false;
    if (!getDelayedTargetsList()
        .equals(other.getDelayedTargetsList())) return false;
    if (getDelay()
        != other.getDelay()) return false;
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
    if (getBlockedTargetsCount() > 0) {
      hash = (37 * hash) + BLOCKED_TARGETS_FIELD_NUMBER;
      hash = (53 * hash) + getBlockedTargetsList().hashCode();
    }
    if (getDelayedTargetsCount() > 0) {
      hash = (37 * hash) + DELAYED_TARGETS_FIELD_NUMBER;
      hash = (53 * hash) + getDelayedTargetsList().hashCode();
    }
    hash = (37 * hash) + DELAY_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getDelay());
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.gbft.framework.data.FaultData parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.gbft.framework.data.FaultData parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.gbft.framework.data.FaultData parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.gbft.framework.data.FaultData parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.gbft.framework.data.FaultData parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.gbft.framework.data.FaultData parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.gbft.framework.data.FaultData parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.gbft.framework.data.FaultData parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.gbft.framework.data.FaultData parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.gbft.framework.data.FaultData parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.gbft.framework.data.FaultData parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.gbft.framework.data.FaultData parseFrom(
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
  public static Builder newBuilder(com.gbft.framework.data.FaultData prototype) {
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
   * Protobuf type {@code FaultData}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:FaultData)
      com.gbft.framework.data.FaultDataOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.gbft.framework.data.Gbft.internal_static_FaultData_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.gbft.framework.data.Gbft.internal_static_FaultData_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.gbft.framework.data.FaultData.class, com.gbft.framework.data.FaultData.Builder.class);
    }

    // Construct using com.gbft.framework.data.FaultData.newBuilder()
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
      blockedTargets_ = emptyIntList();
      delayedTargets_ = emptyIntList();
      delay_ = 0L;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.gbft.framework.data.Gbft.internal_static_FaultData_descriptor;
    }

    @java.lang.Override
    public com.gbft.framework.data.FaultData getDefaultInstanceForType() {
      return com.gbft.framework.data.FaultData.getDefaultInstance();
    }

    @java.lang.Override
    public com.gbft.framework.data.FaultData build() {
      com.gbft.framework.data.FaultData result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.gbft.framework.data.FaultData buildPartial() {
      com.gbft.framework.data.FaultData result = new com.gbft.framework.data.FaultData(this);
      buildPartialRepeatedFields(result);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartialRepeatedFields(com.gbft.framework.data.FaultData result) {
      if (((bitField0_ & 0x00000001) != 0)) {
        blockedTargets_.makeImmutable();
        bitField0_ = (bitField0_ & ~0x00000001);
      }
      result.blockedTargets_ = blockedTargets_;
      if (((bitField0_ & 0x00000002) != 0)) {
        delayedTargets_.makeImmutable();
        bitField0_ = (bitField0_ & ~0x00000002);
      }
      result.delayedTargets_ = delayedTargets_;
    }

    private void buildPartial0(com.gbft.framework.data.FaultData result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.delay_ = delay_;
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
      if (other instanceof com.gbft.framework.data.FaultData) {
        return mergeFrom((com.gbft.framework.data.FaultData)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.gbft.framework.data.FaultData other) {
      if (other == com.gbft.framework.data.FaultData.getDefaultInstance()) return this;
      if (!other.blockedTargets_.isEmpty()) {
        if (blockedTargets_.isEmpty()) {
          blockedTargets_ = other.blockedTargets_;
          bitField0_ = (bitField0_ & ~0x00000001);
        } else {
          ensureBlockedTargetsIsMutable();
          blockedTargets_.addAll(other.blockedTargets_);
        }
        onChanged();
      }
      if (!other.delayedTargets_.isEmpty()) {
        if (delayedTargets_.isEmpty()) {
          delayedTargets_ = other.delayedTargets_;
          bitField0_ = (bitField0_ & ~0x00000002);
        } else {
          ensureDelayedTargetsIsMutable();
          delayedTargets_.addAll(other.delayedTargets_);
        }
        onChanged();
      }
      if (other.getDelay() != 0L) {
        setDelay(other.getDelay());
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
              int v = input.readInt32();
              ensureBlockedTargetsIsMutable();
              blockedTargets_.addInt(v);
              break;
            } // case 8
            case 10: {
              int length = input.readRawVarint32();
              int limit = input.pushLimit(length);
              ensureBlockedTargetsIsMutable();
              while (input.getBytesUntilLimit() > 0) {
                blockedTargets_.addInt(input.readInt32());
              }
              input.popLimit(limit);
              break;
            } // case 10
            case 16: {
              int v = input.readInt32();
              ensureDelayedTargetsIsMutable();
              delayedTargets_.addInt(v);
              break;
            } // case 16
            case 18: {
              int length = input.readRawVarint32();
              int limit = input.pushLimit(length);
              ensureDelayedTargetsIsMutable();
              while (input.getBytesUntilLimit() > 0) {
                delayedTargets_.addInt(input.readInt32());
              }
              input.popLimit(limit);
              break;
            } // case 18
            case 24: {
              delay_ = input.readInt64();
              bitField0_ |= 0x00000004;
              break;
            } // case 24
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

    private com.google.protobuf.Internal.IntList blockedTargets_ = emptyIntList();
    private void ensureBlockedTargetsIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        blockedTargets_ = mutableCopy(blockedTargets_);
        bitField0_ |= 0x00000001;
      }
    }
    /**
     * <code>repeated int32 blocked_targets = 1;</code>
     * @return A list containing the blockedTargets.
     */
    public java.util.List<java.lang.Integer>
        getBlockedTargetsList() {
      return ((bitField0_ & 0x00000001) != 0) ?
               java.util.Collections.unmodifiableList(blockedTargets_) : blockedTargets_;
    }
    /**
     * <code>repeated int32 blocked_targets = 1;</code>
     * @return The count of blockedTargets.
     */
    public int getBlockedTargetsCount() {
      return blockedTargets_.size();
    }
    /**
     * <code>repeated int32 blocked_targets = 1;</code>
     * @param index The index of the element to return.
     * @return The blockedTargets at the given index.
     */
    public int getBlockedTargets(int index) {
      return blockedTargets_.getInt(index);
    }
    /**
     * <code>repeated int32 blocked_targets = 1;</code>
     * @param index The index to set the value at.
     * @param value The blockedTargets to set.
     * @return This builder for chaining.
     */
    public Builder setBlockedTargets(
        int index, int value) {
      
      ensureBlockedTargetsIsMutable();
      blockedTargets_.setInt(index, value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int32 blocked_targets = 1;</code>
     * @param value The blockedTargets to add.
     * @return This builder for chaining.
     */
    public Builder addBlockedTargets(int value) {
      
      ensureBlockedTargetsIsMutable();
      blockedTargets_.addInt(value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int32 blocked_targets = 1;</code>
     * @param values The blockedTargets to add.
     * @return This builder for chaining.
     */
    public Builder addAllBlockedTargets(
        java.lang.Iterable<? extends java.lang.Integer> values) {
      ensureBlockedTargetsIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, blockedTargets_);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int32 blocked_targets = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearBlockedTargets() {
      blockedTargets_ = emptyIntList();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }

    private com.google.protobuf.Internal.IntList delayedTargets_ = emptyIntList();
    private void ensureDelayedTargetsIsMutable() {
      if (!((bitField0_ & 0x00000002) != 0)) {
        delayedTargets_ = mutableCopy(delayedTargets_);
        bitField0_ |= 0x00000002;
      }
    }
    /**
     * <code>repeated int32 delayed_targets = 2;</code>
     * @return A list containing the delayedTargets.
     */
    public java.util.List<java.lang.Integer>
        getDelayedTargetsList() {
      return ((bitField0_ & 0x00000002) != 0) ?
               java.util.Collections.unmodifiableList(delayedTargets_) : delayedTargets_;
    }
    /**
     * <code>repeated int32 delayed_targets = 2;</code>
     * @return The count of delayedTargets.
     */
    public int getDelayedTargetsCount() {
      return delayedTargets_.size();
    }
    /**
     * <code>repeated int32 delayed_targets = 2;</code>
     * @param index The index of the element to return.
     * @return The delayedTargets at the given index.
     */
    public int getDelayedTargets(int index) {
      return delayedTargets_.getInt(index);
    }
    /**
     * <code>repeated int32 delayed_targets = 2;</code>
     * @param index The index to set the value at.
     * @param value The delayedTargets to set.
     * @return This builder for chaining.
     */
    public Builder setDelayedTargets(
        int index, int value) {
      
      ensureDelayedTargetsIsMutable();
      delayedTargets_.setInt(index, value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int32 delayed_targets = 2;</code>
     * @param value The delayedTargets to add.
     * @return This builder for chaining.
     */
    public Builder addDelayedTargets(int value) {
      
      ensureDelayedTargetsIsMutable();
      delayedTargets_.addInt(value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int32 delayed_targets = 2;</code>
     * @param values The delayedTargets to add.
     * @return This builder for chaining.
     */
    public Builder addAllDelayedTargets(
        java.lang.Iterable<? extends java.lang.Integer> values) {
      ensureDelayedTargetsIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, delayedTargets_);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int32 delayed_targets = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearDelayedTargets() {
      delayedTargets_ = emptyIntList();
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }

    private long delay_ ;
    /**
     * <code>int64 delay = 3;</code>
     * @return The delay.
     */
    @java.lang.Override
    public long getDelay() {
      return delay_;
    }
    /**
     * <code>int64 delay = 3;</code>
     * @param value The delay to set.
     * @return This builder for chaining.
     */
    public Builder setDelay(long value) {
      
      delay_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>int64 delay = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearDelay() {
      bitField0_ = (bitField0_ & ~0x00000004);
      delay_ = 0L;
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


    // @@protoc_insertion_point(builder_scope:FaultData)
  }

  // @@protoc_insertion_point(class_scope:FaultData)
  private static final com.gbft.framework.data.FaultData DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.gbft.framework.data.FaultData();
  }

  public static com.gbft.framework.data.FaultData getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<FaultData>
      PARSER = new com.google.protobuf.AbstractParser<FaultData>() {
    @java.lang.Override
    public FaultData parsePartialFrom(
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

  public static com.google.protobuf.Parser<FaultData> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<FaultData> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.gbft.framework.data.FaultData getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
