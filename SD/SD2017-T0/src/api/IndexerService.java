package api;

/*
 * 
 */
public interface IndexerService {

	void add( String documentId, Document doc );

	void remove( String documentId );
	
}
