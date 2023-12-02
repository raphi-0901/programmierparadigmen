@CodedBy("Raphael")
public class Institute {
    private final OurLinkedList formicariums;
    private final String name;

    @CodedBy("Raphael")
    @SignatureAndAssertions(
            preconditions = "Name must not be null or an empty string",
            postconditions = "Initializes a new Institute with a given name."
    )
    public Institute(String name) {
        this.name = name;
        formicariums = new OurLinkedList();
    }

    @CodedBy("Raphael")
    @SignatureAndAssertions(
            postconditions = "Adds the formicarium to the list if it is not already in there."
    )
    public void add(Formicarium formicarium) {
        if(!(formicariums.contains(formicarium))) {
            formicariums.add(formicarium);
        }
    }
    @CodedBy("Clemens")
    @SignatureAndAssertions(
            postconditions = "returns every formicarium in the institute - used for testing"
    )
    public OurLinkedList getFormicariums() {
        return formicariums;
    }

    @CodedBy("Raphael")
    @SignatureAndAssertions(
            postconditions = "Removes the formicarium from the list if it is in there."
    )
    public void remove(Formicarium formicarium) {
        formicariums.remove(formicarium);
    }

    @CodedBy("Raphael")
    @SignatureAndAssertions(
            postconditions = "Removes the formicarium from the list if it is in there."
    )
    public Formicarium getByName(String name) {
        for(Object item : formicariums) {
            Formicarium formicarium = (Formicarium) item;

            if(formicarium.getName().equals(name)) {
                return formicarium;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Institute ").append(name).append("\n");
        formicariums.forEach(formicarium -> result.append(formicarium.toString()).append("\n"));

        return result.toString();
    }
}
