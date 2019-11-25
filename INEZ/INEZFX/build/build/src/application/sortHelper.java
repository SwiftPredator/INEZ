package application;
/**
 * Helper class for retuning tuples in functions. used in @see Brain
 * @author ironmonkeyapps
 *
 */
public class sortHelper implements Comparable<sortHelper> {

    int index, value;

    sortHelper(int index, int value){
        this.index = index;
        this.value = value;
    }

    @Override
    public int compareTo(sortHelper e) {
        return this.value - e.value;
    }
    @Override
    public boolean equals(Object obj) {
    	sortHelper s = (sortHelper) obj;
    	if(s.value == this.value && s.index == this.index) {
    		return true;
    	}
    	
    	return false;
    }
}

