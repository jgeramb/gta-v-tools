package dev.justix.gtavtools.gui.views;

import dev.justix.gtavtools.gui.components.View;
import dev.justix.gtavtools.tools.Category;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class MainView extends View {

    private final List<CategoryView> components;

    public MainView() {
        super(null, null);

        this.components = new ArrayList<>();

        Arrays.stream(Category.values())
                .forEachOrdered(category -> this.components.add(new CategoryView(this, category)));
    }

    public CategoryView getCategoryView(Category category) {
        return this.components
                .stream()
                .filter(categoryView -> categoryView.getCategory().equals(category))
                .findFirst()
                .orElse(null);
    }

}
