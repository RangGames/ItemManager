package wiki.creeper.itemManager.exception;

public class ItemManagerException extends RuntimeException {
    
    public ItemManagerException(String message) {
        super(message);
    }
    
    public ItemManagerException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static class InvalidItemException extends ItemManagerException {
        public InvalidItemException(String message) {
            super(message);
        }
    }
    
    public static class ExpiredItemException extends ItemManagerException {
        public ExpiredItemException(String message) {
            super(message);
        }
    }
    
    public static class AttributionException extends ItemManagerException {
        public AttributionException(String message) {
            super(message);
        }
    }
    
    public static class InvalidTimeException extends ItemManagerException {
        public InvalidTimeException(String message) {
            super(message);
        }
    }
}