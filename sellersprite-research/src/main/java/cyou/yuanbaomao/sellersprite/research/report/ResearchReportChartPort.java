package cyou.yuanbaomao.sellersprite.research.report;

import java.util.List;

@FunctionalInterface
public interface ResearchReportChartPort {

    List<ResearchReportChart> buildCharts(String jobId);
}
