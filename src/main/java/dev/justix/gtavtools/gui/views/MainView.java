package dev.justix.gtavtools.gui.views;

import dev.justix.gtavtools.gui.components.View;
import dev.justix.gtavtools.tools.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainView extends View {

    private final List<CategoryView> components;

    public MainView() {
        super(null, null);

        this.components = new ArrayList<>();

        Arrays.stream(Category.values())
                .forEachOrdered(category -> components.add(new CategoryView(this, category)));
    }

    @Override
    public List<CategoryView> getComponents() {
        return components;
    }

    public CategoryView getCategoryView(Category category) {
        return components
                .stream()
                .filter(categoryView -> categoryView.getCategory().equals(category))
                .findFirst()
                .orElse(null);
    }

}
