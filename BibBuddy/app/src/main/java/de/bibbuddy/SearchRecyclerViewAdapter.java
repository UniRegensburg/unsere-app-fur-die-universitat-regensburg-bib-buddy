package de.bibbuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.jsoup.Jsoup;

/**
 * The SearchRecyclerViewAdapter provides a binding from the libraryList to the view
 * that is displayed within the RecyclerView of the SearchFragment.
 *
 * @author Claudia Schönherr
 */
public class SearchRecyclerViewAdapter
    extends RecyclerView.Adapter<SearchRecyclerViewAdapter.SearchViewHolder> {

  private final SearchListener listener;
  private List<SearchItem> searchResultList;

  /**
   * Constructor of the SearchRecyclerViewAdapter.
   *
   * @param searchResultList result list of the search
   * @param listener         listener for the interface and callback of the search
   */
  public SearchRecyclerViewAdapter(List<SearchItem> searchResultList, SearchListener listener) {

    this.searchResultList = searchResultList;
    this.listener = listener;
  }

  @NonNull
  @Override
  public SearchRecyclerViewAdapter.SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                       int viewType) {
    // RecyclerView calls this method whenever it needs to create a new ViewHolder.
    // The method creates and initializes the ViewHolder and its associated View,
    // but does not fill in the view's contents—the ViewHolder
    // has not yet been bound to specific data.

    return new SearchViewHolder(LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.list_view_item_search, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
    // Get element from your dataset at this position and replace the contents of the view
    // with that element
    SearchItem searchItem = searchResultList.get(position);

    String name = Jsoup.parse(searchItem.getName()).text();
    if (name.length() > 40) {
      name = name.substring(0, 35) + " ...";
    }

    holder.getTextView().setText(name);
    holder.getImageView().setImageResource(searchItem.getImage());
    holder.getModDateView().setText(searchItem.getModDateStr());

    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        listener.onItemClicked(position);
      }
    });

  }

  @Override
  public int getItemCount() {
    return searchResultList.size();
  }

  public void setSearchResultList(List<SearchItem> searchResultList) {
    this.searchResultList = searchResultList;
  }

  public interface SearchListener {
    void onItemClicked(int position);
  }


  public static class SearchViewHolder extends RecyclerView.ViewHolder {

    private final TextView textView;
    private final ImageView imageView;
    private final TextView modDateView;

    /**
     * The SearchViewHolder describes a search item view and metadata about its place
     * within the RecyclerView of the SearchFragment.
     */
    public SearchViewHolder(@NonNull View itemView) {
      super(itemView);

      textView = itemView.findViewById(R.id.search_result_name);
      imageView = itemView.findViewById(R.id.search_result_icon);
      modDateView = itemView.findViewById(R.id.search_mod_date);
    }

    public TextView getTextView() {
      return textView;
    }

    public ImageView getImageView() {
      return imageView;
    }

    public TextView getModDateView() {
      return modDateView;
    }

    public void setImageView(int image) {
      this.imageView.setImageResource(image);
    }

  }

}
