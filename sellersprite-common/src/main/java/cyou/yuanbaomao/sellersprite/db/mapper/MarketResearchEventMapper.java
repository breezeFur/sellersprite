package cyou.yuanbaomao.sellersprite.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchEvent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface MarketResearchEventMapper extends BaseMapper<MarketResearchEvent> {

    @Select("SELECT job_id FROM market_research_event_stream_lock WHERE job_id = #{jobId}")
    String findEventStreamLock(@Param("jobId") String jobId);

    @Insert("INSERT INTO market_research_event_stream_lock (job_id) VALUES (#{jobId})")
    int insertEventStreamLock(@Param("jobId") String jobId);

    @Select("SELECT job_id FROM market_research_event_stream_lock WHERE job_id = #{jobId} FOR UPDATE")
    String lockEventStream(@Param("jobId") String jobId);
}
