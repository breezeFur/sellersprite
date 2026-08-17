package cyou.yuanbaomao.sellersprite.research.event;

/** 市场调研持久化事件类型。Curation 事件名称保持原值以兼容迁移后的前端。 */
public final class ResearchEventTypes {

    public static final String WORKFLOW_STARTED = "workflow_started";
    public static final String RESEARCH_RETRY_SCHEDULED = "research_retry_scheduled";
    public static final String RESEARCH_NODE_STARTED = "research_node_started";
    public static final String RESEARCH_NODE_PROGRESS = "research_node_progress";
    public static final String RESEARCH_NODE_COMPLETED = "research_node_completed";
    public static final String RESEARCH_NODE_FAILED = "research_node_failed";
    public static final String RESEARCH_NODE_CANCELLED = "research_node_cancelled";
    public static final String RESEARCH_COMPLETED = "research_completed";
    public static final String WORKBOOK_READY = "workbook_ready";
    public static final String ANALYSIS_WAITING_RESEARCH = "analysis_waiting_research";
    public static final String ANALYSIS_QUEUED = "analysis_queued";
    public static final String ANALYSIS_CANCEL_REQUESTED = "analysis_cancel_requested";
    public static final String WORKFLOW_COMPLETED = "workflow_completed";
    public static final String WORKFLOW_FAILED = "workflow_failed";
    public static final String WORKFLOW_CANCELLED = "workflow_cancelled";
    public static final String STAGE_COMPLETED = "stage_completed";
    public static final String PRODUCT_SELECTION_REQUIRED = "product_selection_required";
    public static final String PRODUCT_SELECTION_SUBMITTED = "product_selection_submitted";
    public static final String MARKET_ABANDONED = "market_abandoned";

    public static final String PLAN = "plan";
    public static final String WORKBOOK = "workbook";
    public static final String SHEET_PREPARE = "sheet_prepare";
    public static final String SHEET = "sheet";
    public static final String SHEET_FOCUS = "sheet_focus";
    public static final String SHEET_THINK_DELTA = "sheet_think_delta";
    public static final String SHEET_THINK = "sheet_think";
    public static final String SUMMARY_PREPARE = "summary_prepare";
    public static final String SUMMARY_DELTA = "summary_delta";
    public static final String REPORT_CHART = "report_chart";
    public static final String SUMMARY = "summary";
    public static final String REPORT = "report";
    public static final String DOWNLOAD = "download";
    public static final String DONE = "done";
    public static final String ERROR = "error";

    private ResearchEventTypes() {
    }
}
