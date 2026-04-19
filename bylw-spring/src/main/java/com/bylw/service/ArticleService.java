package com.bylw.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.entity.Article;
import com.bylw.entity.Recipe;
import com.bylw.entity.RecipeFavorite;

import java.util.List;

public interface ArticleService {
    Page<Article> listArticles(Integer pageNum, Integer pageSize, String keyword);
    Article getArticleById(Integer articleId);
    Article save(Article article);
    boolean deleteArticle(Integer articleId);
    boolean updateArticle(Article article);
    boolean incrementReadCount(Integer articleId);

    Page<Recipe> listRecipes(Integer pageNum, Integer pageSize, String keyword, String suitablePeople);
    Recipe getRecipeById(Integer recipeId);
    Recipe saveRecipe(Recipe recipe);
    boolean deleteRecipe(Integer recipeId);
    boolean updateRecipe(Recipe recipe);
    boolean incrementCollectCount(Integer recipeId);

    boolean toggleFavorite(Integer userId, Integer recipeId);
    List<RecipeFavorite> getFavorites(Integer userId);
}
