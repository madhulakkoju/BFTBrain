// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: gbft.proto

package com.gbft.framework.data;

public interface ReportDataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ReportData)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>map&lt;string, .ReportData.ReportItem&gt; report_data = 1;</code>
   */
  int getReportDataCount();
  /**
   * <code>map&lt;string, .ReportData.ReportItem&gt; report_data = 1;</code>
   */
  boolean containsReportData(
      java.lang.String key);
  /**
   * Use {@link #getReportDataMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, com.gbft.framework.data.ReportData.ReportItem>
  getReportData();
  /**
   * <code>map&lt;string, .ReportData.ReportItem&gt; report_data = 1;</code>
   */
  java.util.Map<java.lang.String, com.gbft.framework.data.ReportData.ReportItem>
  getReportDataMap();
  /**
   * <code>map&lt;string, .ReportData.ReportItem&gt; report_data = 1;</code>
   */

  /* nullable */
com.gbft.framework.data.ReportData.ReportItem getReportDataOrDefault(
      java.lang.String key,
      /* nullable */
com.gbft.framework.data.ReportData.ReportItem defaultValue);
  /**
   * <code>map&lt;string, .ReportData.ReportItem&gt; report_data = 1;</code>
   */

  com.gbft.framework.data.ReportData.ReportItem getReportDataOrThrow(
      java.lang.String key);
}
