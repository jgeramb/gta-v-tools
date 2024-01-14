package dev.justix.gtavtools.util.images;

public record ComparisonResult(int checked, int matches) {

    public double getPercentage() {
        if(this.checked == 0 || this.matches == 0)
            return 0d;

        return (double) this.matches / this.checked;
    }

}
