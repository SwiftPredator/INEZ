package application;

public class sortHelper implements Comparable<sortHelper> {

    int index, value;

    sortHelper(int index, int value){
        this.index = index;
        this.value = value;
    }

    public int compareTo(sortHelper e) {
        return this.value - e.value;
    }
}

