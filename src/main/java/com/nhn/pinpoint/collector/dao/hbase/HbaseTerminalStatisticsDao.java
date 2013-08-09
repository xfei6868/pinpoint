package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.*;
import static com.nhn.pinpoint.common.hbase.HBaseTables.TERMINAL_STATISTICS_CF_COUNTER;

import com.nhn.pinpoint.collector.dao.TerminalStatisticsDao;
import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.TerminalSpanUtils;
import com.nhn.pinpoint.common.util.TimeSlot;
import org.springframework.stereotype.Repository;

/**
 * @author netspider
 */
@Deprecated
@Repository
public class HbaseTerminalStatisticsDao implements TerminalStatisticsDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

    @Autowired
    private AcceptedTimeService acceptedTimeService;

	/**
	 * 
	 * @param sourceApplicationName
	 *            조회해야 되는 applicationName. applicatioName을 기준으로 단말노드의 데이터를 읽어온다.
	 * @param destApplicationName
	 * @param destServiceType
	 * @param destHost
	 * @param elapsed
	 * @param isError
	 */
	@Override
	public void update(String sourceApplicationName, String destApplicationName, short destServiceType, String destHost, int elapsed, boolean isError) {
		if (logger.isDebugEnabled()) {
			logger.debug("[UpdatingTerminalStatistics] " + sourceApplicationName + " -> " + destApplicationName + " (" + ServiceType.findServiceType(destServiceType) + ")");
		}
        if (destHost == null) {
//            httpclient와 같은 경우는 endpoint가 없을수 있다.
            destHost = "";
        }
		byte[] columnName = TerminalSpanUtils.makeColumnName(destServiceType, destApplicationName, destHost, elapsed, isError);

		// make row key
		long acceptedTime = acceptedTimeService.getAcceptedTime();
		long rowTimeSlot = TimeSlot.getStatisticsRowSlot(acceptedTime);
		final byte[] rowKey = TerminalSpanUtils.makeRowKey(sourceApplicationName, rowTimeSlot);

		hbaseTemplate.incrementColumnValue(TERMINAL_STATISTICS, rowKey, TERMINAL_STATISTICS_CF_COUNTER, columnName, 1L);
	}
}
