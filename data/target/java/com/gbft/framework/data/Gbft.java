// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: gbft.proto

package com.gbft.framework.data;

public final class Gbft {
  private Gbft() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_RequestData_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_RequestData_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_MessageData_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MessageData_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_MessageData_ReplyDataEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MessageData_ReplyDataEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_MessageData_ExtraValuesEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MessageData_ExtraValuesEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_MessageData_ExtraDataEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MessageData_ExtraDataEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_SwitchingData_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_SwitchingData_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_LearningData_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_LearningData_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_LearningData_ReportEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_LearningData_ReportEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_FetchData_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_FetchData_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_FetchData_ServiceStateEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_FetchData_ServiceStateEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_FaultData_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_FaultData_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_MessageBlock_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MessageBlock_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Event_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_Event_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_UnitData_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_UnitData_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ConfigData_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ConfigData_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ConfigData_DataEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ConfigData_DataEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_PluginData_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_PluginData_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ReportData_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ReportData_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ReportData_ReportDataEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ReportData_ReportDataEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ReportData_ReportItem_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ReportData_ReportItem_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ReportData_ReportItem_ItemDataEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ReportData_ReportItem_ItemDataEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\ngbft.proto\032\037google/protobuf/timestamp." +
      "proto\032\033google/protobuf/empty.proto\"\216\003\n\013R" +
      "equestData\022\016\n\006client\030\001 \001(\005\022\023\n\013request_nu" +
      "m\030\002 \001(\003\022\016\n\006record\030\003 \001(\005\022)\n\toperation\030\004 \001" +
      "(\0162\026.RequestData.Operation\022\r\n\005value\030\005 \001(" +
      "\005\022-\n\ttimestamp\030\006 \001(\0132\032.google.protobuf.T" +
      "imestamp\022$\n\rreport_quorum\030\007 \003(\0132\r.Learni" +
      "ngData\022\025\n\rrequest_dummy\030\010 \001(\014\022\026\n\016compute" +
      "_factor\030\t \001(\005\022\022\n\nreply_size\030\n \001(\005\022\031\n\021ear" +
      "ly_exec_result\030\013 \001(\005\022\024\n\014is_tnx_valid\030\014 \001" +
      "(\010\"G\n\tOperation\022\007\n\003NOP\020\000\022\007\n\003ADD\020\001\022\007\n\003SUB" +
      "\020\002\022\007\n\003INC\020\003\022\007\n\003DEC\020\004\022\r\n\tREAD_ONLY\020\005\"\375\005\n\013" +
      "MessageData\022\024\n\014sequence_num\030\001 \001(\003\022\020\n\010vie" +
      "w_num\030\002 \001(\003\022\024\n\014message_type\030\003 \001(\005\022\016\n\006sou" +
      "rce\030\004 \001(\005\022\017\n\007targets\030\005 \003(\005\022\036\n\010requests\030\006" +
      " \003(\0132\014.RequestData\022\024\n\014request_nums\030\007 \003(\003" +
      "\022\030\n\020has_request_data\030\010 \001(\010\022\016\n\006digest\030\t \001" +
      "(\014\022\r\n\005flags\030\n \003(\005\022/\n\nreply_data\030\013 \003(\0132\033." +
      "MessageData.ReplyDataEntry\0223\n\014extra_valu" +
      "es\030\014 \003(\0132\035.MessageData.ExtraValuesEntry\022" +
      "/\n\nextra_data\030\r \003(\0132\033.MessageData.ExtraD" +
      "ataEntry\022-\n\ttimestamp\030\016 \001(\0132\032.google.pro" +
      "tobuf.Timestamp\022\031\n\005fault\030\017 \001(\0132\n.FaultDa" +
      "ta\022\031\n\005fetch\030\020 \001(\0132\n.FetchData\022\035\n\006report\030" +
      "\021 \001(\0132\r.LearningData\022\036\n\006switch\030\022 \001(\0132\016.S" +
      "witchingData\022\032\n\022aggregation_values\030\023 \003(\003" +
      "\022\036\n\026is_endorsement_request\030\024 \001(\010\022\021\n\txov_" +
      "state\030\025 \001(\005\0320\n\016ReplyDataEntry\022\013\n\003key\030\001 \001" +
      "(\003\022\r\n\005value\030\002 \001(\005:\0028\001\0322\n\020ExtraValuesEntr" +
      "y\022\013\n\003key\030\001 \001(\005\022\r\n\005value\030\002 \001(\003:\0028\001\0320\n\016Ext" +
      "raDataEntry\022\013\n\003key\030\001 \001(\005\022\r\n\005value\030\002 \001(\014:" +
      "\0028\001\"<\n\rSwitchingData\022\024\n\014switch_ready\030\001 \001" +
      "(\010\022\025\n\rnext_protocol\030\002 \001(\t\"\177\n\014LearningDat" +
      "a\022)\n\006report\030\001 \003(\0132\031.LearningData.ReportE" +
      "ntry\022\025\n\rnext_protocol\030\002 \001(\t\032-\n\013ReportEnt" +
      "ry\022\013\n\003key\030\001 \001(\005\022\r\n\005value\030\002 \001(\002:\0028\001\"\211\001\n\tF" +
      "etchData\022\022\n\nis_request\030\001 \001(\010\0223\n\rservice_" +
      "state\030\002 \003(\0132\034.FetchData.ServiceStateEntr" +
      "y\0323\n\021ServiceStateEntry\022\013\n\003key\030\001 \001(\005\022\r\n\005v" +
      "alue\030\002 \001(\005:\0028\001\"L\n\tFaultData\022\027\n\017blocked_t" +
      "argets\030\001 \003(\005\022\027\n\017delayed_targets\030\002 \003(\005\022\r\n" +
      "\005delay\030\003 \001(\003\"2\n\014MessageBlock\022\"\n\014message_" +
      "data\030\006 \003(\0132\014.MessageData\"\263\003\n\005Event\022$\n\nev" +
      "ent_type\030\001 \001(\0162\020.Event.EventType\022\036\n\tunit" +
      "_data\030\002 \001(\0132\t.UnitDataH\000\022\"\n\013config_data\030" +
      "\003 \001(\0132\013.ConfigDataH\000\022\"\n\013plugin_data\030\004 \001(" +
      "\0132\013.PluginDataH\000\022\"\n\013report_data\030\005 \001(\0132\013." +
      "ReportDataH\000\022&\n\rmessage_block\030\006 \001(\0132\r.Me" +
      "ssageBlockH\000\022\020\n\006target\030\007 \001(\005H\000\"\257\001\n\tEvent" +
      "Type\022\010\n\004INIT\020\000\022\n\n\006CONFIG\020\001\022\t\n\005READY\020\002\022\017\n" +
      "\013PLUGIN_INIT\020\003\022\t\n\005START\020\004\022\t\n\005BLOCK\020\005\022\010\n\004" +
      "STOP\020\006\022\010\n\004EXIT\020\007\022\023\n\017BENCHMARK_START\020\010\022\024\n" +
      "\020BENCHMARK_REPORT\020\t\022\013\n\007MESSAGE\020\n\022\016\n\nCONN" +
      "ECTION\020\013B\014\n\nevent_data\"B\n\010UnitData\022\014\n\004un" +
      "it\030\001 \001(\005\022\022\n\nnode_count\030\002 \001(\005\022\024\n\014client_c" +
      "ount\030\003 \001(\005\"\221\001\n\nConfigData\022#\n\004data\030\001 \003(\0132" +
      "\025.ConfigData.DataEntry\022\027\n\017defaultProtoco" +
      "l\030\002 \001(\t\022\030\n\005units\030\003 \003(\0132\t.UnitData\032+\n\tDat" +
      "aEntry\022\013\n\003key\030\001 \001(\t\022\r\n\005value\030\002 \001(\t:\0028\001\"f" +
      "\n\nPluginData\022\023\n\013plugin_name\030\001 \001(\t\022\024\n\014mes" +
      "sage_type\030\002 \001(\005\022\014\n\004data\030\003 \001(\014\022\016\n\006source\030" +
      "\004 \001(\005\022\017\n\007targets\030\005 \003(\005\"\201\002\n\nReportData\0220\n" +
      "\013report_data\030\001 \003(\0132\033.ReportData.ReportDa" +
      "taEntry\032I\n\017ReportDataEntry\022\013\n\003key\030\001 \001(\t\022" +
      "%\n\005value\030\002 \001(\0132\026.ReportData.ReportItem:\002" +
      "8\001\032v\n\nReportItem\0227\n\titem_data\030\002 \003(\0132$.Re" +
      "portData.ReportItem.ItemDataEntry\032/\n\rIte" +
      "mDataEntry\022\013\n\003key\030\001 \001(\t\022\r\n\005value\030\002 \001(\t:\002" +
      "8\0012F\n\nEntityComm\0228\n\rsend_decision\022\r.Lear" +
      "ningData\032\026.google.protobuf.Empty\"\0002A\n\tAg" +
      "entComm\0224\n\tsend_data\022\r.LearningData\032\026.go" +
      "ogle.protobuf.Empty\"\000B\033\n\027com.gbft.framew" +
      "ork.dataP\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
          com.google.protobuf.EmptyProto.getDescriptor(),
        });
    internal_static_RequestData_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_RequestData_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_RequestData_descriptor,
        new java.lang.String[] { "Client", "RequestNum", "Record", "Operation", "Value", "Timestamp", "ReportQuorum", "RequestDummy", "ComputeFactor", "ReplySize", "EarlyExecResult", "IsTnxValid", });
    internal_static_MessageData_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_MessageData_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_MessageData_descriptor,
        new java.lang.String[] { "SequenceNum", "ViewNum", "MessageType", "Source", "Targets", "Requests", "RequestNums", "HasRequestData", "Digest", "Flags", "ReplyData", "ExtraValues", "ExtraData", "Timestamp", "Fault", "Fetch", "Report", "Switch", "AggregationValues", "IsEndorsementRequest", "XovState", });
    internal_static_MessageData_ReplyDataEntry_descriptor =
      internal_static_MessageData_descriptor.getNestedTypes().get(0);
    internal_static_MessageData_ReplyDataEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_MessageData_ReplyDataEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_MessageData_ExtraValuesEntry_descriptor =
      internal_static_MessageData_descriptor.getNestedTypes().get(1);
    internal_static_MessageData_ExtraValuesEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_MessageData_ExtraValuesEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_MessageData_ExtraDataEntry_descriptor =
      internal_static_MessageData_descriptor.getNestedTypes().get(2);
    internal_static_MessageData_ExtraDataEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_MessageData_ExtraDataEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_SwitchingData_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_SwitchingData_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_SwitchingData_descriptor,
        new java.lang.String[] { "SwitchReady", "NextProtocol", });
    internal_static_LearningData_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_LearningData_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_LearningData_descriptor,
        new java.lang.String[] { "Report", "NextProtocol", });
    internal_static_LearningData_ReportEntry_descriptor =
      internal_static_LearningData_descriptor.getNestedTypes().get(0);
    internal_static_LearningData_ReportEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_LearningData_ReportEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_FetchData_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_FetchData_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_FetchData_descriptor,
        new java.lang.String[] { "IsRequest", "ServiceState", });
    internal_static_FetchData_ServiceStateEntry_descriptor =
      internal_static_FetchData_descriptor.getNestedTypes().get(0);
    internal_static_FetchData_ServiceStateEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_FetchData_ServiceStateEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_FaultData_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_FaultData_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_FaultData_descriptor,
        new java.lang.String[] { "BlockedTargets", "DelayedTargets", "Delay", });
    internal_static_MessageBlock_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_MessageBlock_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_MessageBlock_descriptor,
        new java.lang.String[] { "MessageData", });
    internal_static_Event_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_Event_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_Event_descriptor,
        new java.lang.String[] { "EventType", "UnitData", "ConfigData", "PluginData", "ReportData", "MessageBlock", "Target", "EventData", });
    internal_static_UnitData_descriptor =
      getDescriptor().getMessageTypes().get(8);
    internal_static_UnitData_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_UnitData_descriptor,
        new java.lang.String[] { "Unit", "NodeCount", "ClientCount", });
    internal_static_ConfigData_descriptor =
      getDescriptor().getMessageTypes().get(9);
    internal_static_ConfigData_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ConfigData_descriptor,
        new java.lang.String[] { "Data", "DefaultProtocol", "Units", });
    internal_static_ConfigData_DataEntry_descriptor =
      internal_static_ConfigData_descriptor.getNestedTypes().get(0);
    internal_static_ConfigData_DataEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ConfigData_DataEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_PluginData_descriptor =
      getDescriptor().getMessageTypes().get(10);
    internal_static_PluginData_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_PluginData_descriptor,
        new java.lang.String[] { "PluginName", "MessageType", "Data", "Source", "Targets", });
    internal_static_ReportData_descriptor =
      getDescriptor().getMessageTypes().get(11);
    internal_static_ReportData_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ReportData_descriptor,
        new java.lang.String[] { "ReportData", });
    internal_static_ReportData_ReportDataEntry_descriptor =
      internal_static_ReportData_descriptor.getNestedTypes().get(0);
    internal_static_ReportData_ReportDataEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ReportData_ReportDataEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_ReportData_ReportItem_descriptor =
      internal_static_ReportData_descriptor.getNestedTypes().get(1);
    internal_static_ReportData_ReportItem_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ReportData_ReportItem_descriptor,
        new java.lang.String[] { "ItemData", });
    internal_static_ReportData_ReportItem_ItemDataEntry_descriptor =
      internal_static_ReportData_ReportItem_descriptor.getNestedTypes().get(0);
    internal_static_ReportData_ReportItem_ItemDataEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ReportData_ReportItem_ItemDataEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    com.google.protobuf.TimestampProto.getDescriptor();
    com.google.protobuf.EmptyProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}