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

    return new SearchViewHolder(LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.list_view_item_search, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
    SearchItem searchItem = searchResultList.get(position);

    holder.getResultName().setText(searchItem.getDisplayName());
    holder.getResultIcon().setImageResource(searchItem.getImage());
    holder.getModDate().setText(searchItem.getModDateStr());

    holder.itemView.setOnClickListener(v -> listener.onItemClicked(position));
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

    private final TextView resultName;
    private final ImageView resultIcon;
    private final TextView modDate;

    /**
     * The SearchViewHolder describes a search item view and metadata about its place
     * within the RecyclerView of the SearchFragment.
     */
    public SearchViewHolder(@NonNull View itemView) {
      super(itemView);

      resultName = itemView.findViewById(R.id.search_result_name);
      resultIcon = itemView.findViewById(R.id.search_result_icon);
      modDate = itemView.findViewById(R.id.search_mod_date);
    }

    public TextView getResultName() {
      return resultName;
    }

    public ImageView getResultIcon() {
      return resultIcon;
    }

    public TextView getModDate() {
      return modDate;
    }

  }

}
