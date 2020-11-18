package tas_fa20;


public class Badge {

    private String id;
    private String description;
    
    public Badge(String id, String description){
        this.id = id;
        this.description = description;
    }
    
    public String getID(){
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString(){
        StringBuilder badge = new StringBuilder();
        
        badge.append("#").append(id).append(" ");
        badge.append("(").append(description).append(")");
        
        return(badge.toString());
    }
    
    
    /* Alternatively titled method aliases for Feature Test compatability */
    
    public String getId() {
        return getID();
    }
    
}
