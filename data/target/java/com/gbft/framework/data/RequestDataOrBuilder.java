// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: gbft.proto

package com.gbft.framework.data;

public interface RequestDataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:RequestData)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int32 client = 1;</code>
   * @return The client.
   */
  int getClient();

  /**
   * <code>int64 request_num = 2;</code>
   * @return The requestNum.
   */
  long getRequestNum();

  /**
   * <code>int32 record = 3;</code>
   * @return The record.
   */
  int getRecord();

  /**
   * <code>.RequestData.Operation operation = 4;</code>
   * @return The enum numeric value on the wire for operation.
   */
  int getOperationValue();
  /**
   * <code>.RequestData.Operation operation = 4;</code>
   * @return The operation.
   */
  com.gbft.framework.data.RequestData.Operation getOperation();

  /**
   * <code>int32 value = 5;</code>
   * @return The value.
   */
  int getValue();

  /**
   * <code>.google.protobuf.Timestamp timestamp = 6;</code>
   * @return Whether the timestamp field is set.
   */
  boolean hasTimestamp();
  /**
   * <code>.google.protobuf.Timestamp timestamp = 6;</code>
   * @return The timestamp.
   */
  com.google.protobuf.Timestamp getTimestamp();
  /**
   * <code>.google.protobuf.Timestamp timestamp = 6;</code>
   */
  com.google.protobuf.TimestampOrBuilder getTimestampOrBuilder();

  /**
   * <code>repeated .LearningData report_quorum = 7;</code>
   */
  java.util.List<com.gbft.framework.data.LearningData> 
      getReportQuorumList();
  /**
   * <code>repeated .LearningData report_quorum = 7;</code>
   */
  com.gbft.framework.data.LearningData getReportQuorum(int index);
  /**
   * <code>repeated .LearningData report_quorum = 7;</code>
   */
  int getReportQuorumCount();
  /**
   * <code>repeated .LearningData report_quorum = 7;</code>
   */
  java.util.List<? extends com.gbft.framework.data.LearningDataOrBuilder> 
      getReportQuorumOrBuilderList();
  /**
   * <code>repeated .LearningData report_quorum = 7;</code>
   */
  com.gbft.framework.data.LearningDataOrBuilder getReportQuorumOrBuilder(
      int index);

  /**
   * <code>bytes request_dummy = 8;</code>
   * @return The requestDummy.
   */
  com.google.protobuf.ByteString getRequestDummy();

  /**
   * <code>int32 compute_factor = 9;</code>
   * @return The computeFactor.
   */
  int getComputeFactor();

  /**
   * <code>int32 reply_size = 10;</code>
   * @return The replySize.
   */
  int getReplySize();

  /**
   * <code>int32 early_exec_result = 11;</code>
   * @return The earlyExecResult.
   */
  int getEarlyExecResult();

  /**
   * <code>bool is_tnx_valid = 12;</code>
   * @return The isTnxValid.
   */
  boolean getIsTnxValid();

  /**
   * <code>int64 curr_version = 13;</code>
   * @return The currVersion.
   */
  long getCurrVersion();
}
