package com.alvazan.orm.api.z5api;

import java.util.Collection;
import java.util.List;

import com.alvazan.orm.api.z8spi.Key;
import com.alvazan.orm.api.z8spi.KeyValue;
import com.alvazan.orm.api.z8spi.MetaLookup;
import com.alvazan.orm.api.z8spi.NoSqlRawSession;
import com.alvazan.orm.api.z8spi.Row;
import com.alvazan.orm.api.z8spi.ScanInfo;
import com.alvazan.orm.api.z8spi.action.Column;
import com.alvazan.orm.api.z8spi.action.IndexColumn;
import com.alvazan.orm.api.z8spi.iter.AbstractCursor;
import com.alvazan.orm.layer5.nosql.cache.NoSqlWriteCacheImpl;
import com.google.inject.ImplementedBy;

@ImplementedBy(NoSqlWriteCacheImpl.class)
public interface NoSqlSession {

	/**
	 * Retrieves the rawest interface that all the providers implement(in-memory, cassandra, hadoop, etc) BUT
	 * be warned, when you call persist, it is executed IMMEDIATELY, there is no flush method on that
	 * interface.  In fact, the flush methods of all higher level interfaces call persist ONCE on the raw
	 * session and providers are supposed to do ONE send to avoid network latency if you are sending 100
	 * actions to the nosql database.
	 * @return
	 */
	public NoSqlRawSession getRawSession();
	
	public void persistIndex(String colFamily, String indexColFamily, byte[] rowKey, IndexColumn column);
	public void removeFromIndex(String colFamily, String indexColFamily, byte[] rowKeyBytes, IndexColumn c);
	
	public void put(String colFamily, byte[] rowKey, List<Column> columns);

	/**
	 * Remove entire row.
	 * 
	 * @param colFamily
	 * @param rowKey
	 */
	public void remove(String colFamily, byte[] rowKey);
	
	/**
	 * Remove specific columns from a row, mainly 
	 * 
	 * @param colFamily
	 * @param rowKey
	 * @param columns
	 */
	public void remove(String colFamily, byte[] rowKey, Collection<byte[]> columnNames);
	
	public AbstractCursor<KeyValue<Row>> findAll(String colFamily, Iterable<byte[]> rowKeys, boolean skipCache);
	
	public Row find(String colFamily, byte[] rowKey);
	
	public void flush();

	public void clearDb();

	/**
	 * Returns a special Iterable loads itself first with "batchSize" records and then after you
	 * iterator over those and request "batchSize+1" record, it hits the database again such that
	 * your memory does not explode if you have a huge amount of records.
	 * @param from
	 * @param to
	 * @param batchSize 
	 * @param colFamily
	 * @param rowKey
	 * @return
	 */
	public AbstractCursor<IndexColumn> scanIndex(ScanInfo info, Key from, Key to, Integer batchSize);
	public AbstractCursor<IndexColumn> scanIndex(ScanInfo scanInfo, List<byte[]> values);
	
	public AbstractCursor<Column> columnSlice(String colFamily, byte[] rowKey, byte[] from, byte[] to, Integer batchSize);
	
	public void setOrmSessionForMeta(MetaLookup entityMgr);

}