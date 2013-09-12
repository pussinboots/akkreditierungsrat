package org.akkreditierung.ui.model;

import java.io.Serializable;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebean.QueryIterator;
import com.avaje.ebean.Transaction;

/**
 * Abstract class for all sql persistent beans that implements some common functionalities.
 * 
 * @author fit
 * 
 * @param <T>
 *            the generic class type of the db entities
 */
public abstract class DefaultBean<T> implements Serializable {

	public static String DB_ADVERTISER_CONFIG = "akkreditierungsrat";
	
    /**
     * 
     */
    private static enum Change {
        INSERT,
        SAVE,
        UPDATE,
        DELETE
    };

    private static final long serialVersionUID = 5517860393924994051L;

    private final String      dbName;

    private final Class<T>    entityClass;

    /**
     * Default constructor.
     * 
     * @param dbName
     *            the name of the database to use (see ebean.properties)
     * @param entityClass
     *            the class of the entity ( determinate the sql table to use)
     */
    public DefaultBean(String dbName, Class<T> entityClass) {
        this.dbName = dbName;
        this.entityClass = entityClass;
    }

    /**
     * Returns an instance of the {@link com.avaje.ebean.EbeanServer} for the specified database name {@link #dbName}.
     * 
     * @return an instance of the {@link com.avaje.ebean.EbeanServer}
     */
    public EbeanServer getServer() {
        return Ebean.getServer(dbName);
    }

    /**
     * Returns an instance of the {@link com.avaje.ebean.EbeanServer} for the specified database name {@link #dbName}.
     * 
     * @return an instance of the {@link com.avaje.ebean.EbeanServer}
     */
    public Query<T> getQuery() {
        return Ebean.getServer(dbName).find(entityClass);
    }

    /**
     * Returns an instance of the {@link com.avaje.ebean.EbeanServer} for the specified database name {@link #dbName}.
     * 
     * @param query
     *            the sql query
     * 
     * @return an instance of the {@link com.avaje.ebean.EbeanServer}
     */
    public Query<T> getQuery(String query) {
        return Ebean.getServer(dbName).createQuery(entityClass, query);
    }

    /**
     * Find one instance of T by the passed primary key object.
     * 
     * @param primaryKey
     *            the primary key of the entity from type T
     * @return a instance of T or null
     */
    public T getByPrimaryKey(Object primaryKey) {
        return getServer().find(entityClass, primaryKey);
    }

    /**
     * Returns iterator for the complete table data. How many object are load into memory can be specified with the
     * parameter fetchSize.
     * 
     * @param fetchSize
     *            specify the fetch size count of objects that will be read with one select
     * @return an instance of {@link com.avaje.ebean.QueryIterator}
     */
    public QueryIterator<T> iteratAll(int fetchSize) {
        return getServer().find(entityClass).setBufferFetchSizeHint(fetchSize).findIterate();
    }

    /**
     * Returns the count entities from the database table.
     * 
     * @return result from SELECT COUNT(*) command
     */
    public int countAll() {
        return getServer().find(entityClass).findRowCount();
    }

    /**
     * Returns iterator for the complete table data. Use default fetchSize of 25 {@link #iteratAll(int)}.
     * 
     * @return an instance of {@link com.avaje.ebean.QueryIterator}
     */
    public QueryIterator<T> iteratAll() {
        return iteratAll(25);
    }

    /**
     * Perform database update for the passed entity instance.
     * 
     * @param entity
     *            the entity to update
     */
    public void update(T entity) {
        getServer().update(entity);
        postChange(Change.UPDATE, entity);
    }

    /**
     * Perform database delete for the passed entity instance.
     * 
     * @param entity
     *            the entity to delete
     */
    public void delete(T entity) {
        getServer().delete(entity);
        postChange(Change.DELETE, entity);
    }

    /**
     * Perform database insert/update for the passed entity instance.
     * 
     * @param entity
     *            the entity to insert/update
     */
    public void save(T entity) {
        getServer().save(entity);
        postChange(Change.SAVE, entity);
    }

    /**
     * Perform database insert for the passed entity instance.
     * 
     * @param entity
     *            the entity to insert
     */
    public void insert(T entity) {
        getServer().insert(entity);
        postChange(Change.INSERT, entity);
    }

    /**
     * Perform database insert for the passed entity instance. The insert command and the call of
     * {@link #postSave(Object)} is performed in one database transaction.
     * 
     * @param entity
     *            the entity to insert
     * @throws Exception
     *             can be occur during the transaction. if it is so than the transaction will be rollbacked
     */
    public void safeSave(T entity) throws Exception {
        Transaction transaction = null;
        try {
            transaction = getServer().beginTransaction();
            getServer().save(entity);
            postChange(Change.SAVE, entity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    /**
     * Is called from all database changed method like save, delete update and call one of the specific change notifier
     * method like {@link #postDelete(Object)}.
     * 
     * @param changeEvent
     *            the change event type depends on the caller method if it was save than this value is
     *            {@link org.akkreditierung.ui.model.DefaultBean.Change#SAVE}
     * @param changedEntity
     *            the entity instance that was passed to one of the database modified method
     */
    private void postChange(Change changeEvent, T changedEntity) {
        postChanged();
        switch (changeEvent) {
            case SAVE:
                postSave(changedEntity);
                return;
            case DELETE:
                postDelete(changedEntity);
                return;
            case UPDATE:
                postUpdate(changedEntity);
                return;
            default:
                break;
        }
    }

    /**
     * Default implementation did nothing. Is called for any database action like update, save, delete. Can be override
     * to perform some code after successfully changes like clearing caches logging and so on.
     */
    public void postChanged() {
        // Default NOP
    }

    /**
     * Default implementation did nothing. Is called from the implemented {@link #delete(Object)} method after the
     * passed entity was deleted. Can be override to perform some code after the entity was successfully deleted.
     * 
     * @param deletedEntity
     *            the passed entity from the previous {@link #delete(Object)} method call
     */
    private void postDelete(T deletedEntity) {
        // Default NOP
    }

    /**
     * Default implementation did nothing. Is called from the implemented {@link #update(Object)} method after the
     * passed entity was updated. Can be override to perform some code after the entity was successfully updated.
     * 
     * @param updatedEntity
     *            the passed entity from the previous {@link #update(Object)} method call
     */
    private void postUpdate(T updatedEntity) {
        // Default NOP
    }

    /**
     * Default implementation did nothing. Is called from the implemented {@link #save(Object)} of
     * {@link #safeSave(Object)} method after the passed entity was persisted. Can be override to perform some code
     * after the entity was successfully persisted.
     * 
     * @param savedEntity
     *            the passed entity from the previous {@link #save(Object)} or {@link #safeSave(Object)} method call
     */
    public void postSave(T savedEntity) {
        // Default NOP
    }
}
