package de.bibbuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

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
   * @param searchResultList searchList TODO
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

    holder.getTextView().setText(searchItem.getName());
    holder.getImageView().setImageResource(searchItem.getImage());

    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        listener.onItemClicked(position);
      }
    });

  }

  @Override
  public int getItemCount() {
    // RecyclerView calls this method to get the size of the data set.
    // RecyclerView uses this to determine when there are no more items that can be displayed.
    return searchResultList.size();
  }

  public void setSearchResultList(List<SearchItem> searchResultList) {
    this.searchResultList = searchResultList;
  }

  public interface SearchListener { // create an interface
    void onItemClicked(int position); // create callback function
  }


  public static class SearchViewHolder extends RecyclerView.ViewHolder {

    private final TextView textView;
    private final ImageView imageView;

    /**
     * The SearchViewHolder describes a search item view and metadata about its place
     * within the RecyclerView of the SearchFragment.
     */
    public SearchViewHolder(@NonNull View itemView) {
      super(itemView);

      textView = itemView.findViewById(R.id.search_result_name);
      imageView = itemView.findViewById(R.id.search_result_icon);
    }

    public TextView getTextView() {
      return textView;
    }

    public ImageView getImageView() {
      return imageView;
    }

    public void setImageView(int image) {
      this.imageView.setImageResource(image);
    }

  }
}