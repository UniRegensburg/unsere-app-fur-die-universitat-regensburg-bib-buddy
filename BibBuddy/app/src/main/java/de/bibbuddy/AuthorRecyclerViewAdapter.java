package de.bibbuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AuthorRecyclerViewAdapter
    extends RecyclerView.Adapter<AuthorRecyclerViewAdapter.AuthorViewHolder> {
  private final AuthorRecyclerViewAdapter.AuthorListener listener;
  private final AuthorAdapterDataObserver dataObserver = new AuthorAdapterDataObserver();
  private final List<Author> authorList;
  private List<AuthorItem> authorItemList;

  /**
   * AuthorRecyclerViewAdapter provides a binding from the authorList to the view
   * that is displayed within the RecyclerView of the AuthorFragment.
   *
   * @author Sarah Kurek
   */
  public AuthorRecyclerViewAdapter(AuthorListener listener,
                                   List<Author> authorList) {
    this.listener = listener;
    this.authorList = authorList;
    updateAuthorItemList();
  }

  private void updateAuthorItemList() {
    authorItemList = new ArrayList<>();

    for (Author author : authorList) {
      authorItemList.add(new AuthorItem(author));
    }
  }

  @NonNull
  @Override
  public AuthorRecyclerViewAdapter.AuthorViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                       int viewType) {
    return new AuthorViewHolder(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.list_view_item_author, parent, false));
  }

  @Override
  public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);

    registerAdapterDataObserver(dataObserver);
  }

  @Override
  public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
    super.onDetachedFromRecyclerView(recyclerView);

    unregisterAdapterDataObserver(dataObserver);
  }

  @Override
  public void onBindViewHolder(@NonNull AuthorRecyclerViewAdapter.AuthorViewHolder holder,
                               int position) {
    AuthorItem authorItem = authorItemList.get(position);
    String name = authorItem.getFirstName() + " " + authorItem.getLastName();

    holder.getTextNameView().setText(name);
    holder.getImageAuthorView().setImageResource(authorItem.getImage());
    holder.getTextTitleView().setText(authorItem.getTitle());

    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        listener.onItemClicked(position);
      }
    });

    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        if (position == RecyclerView.NO_POSITION) {
          return false;
        }

        listener.onLongItemClicked(position, authorItem, v);
        return true;
      }
    });
  }

  @Override
  public int getItemCount() {
    return authorItemList.size();
  }

  public interface AuthorListener {
    void onItemClicked(int position);

    void onLongItemClicked(int position, AuthorItem authorItem, View view);
  }

  public static class AuthorViewHolder extends RecyclerView.ViewHolder {

    private final TextView textNameView;
    private final ImageView imageAuthorView;
    private final TextView textTitleView;

    /**
     * Custom ViewHolder constructor to setup its basic view.
     *
     * @param itemView View of the AuthorRecyclerView-item.
     * @author Sarah
     */
    public AuthorViewHolder(@NonNull View itemView) {
      super(itemView);

      textNameView = itemView.findViewById(R.id.author_name);
      imageAuthorView = itemView.findViewById(R.id.person_icon);
      textTitleView = itemView.findViewById(R.id.author_title);
    }

    public TextView getTextNameView() {
      return textNameView;
    }

    public ImageView getImageAuthorView() {
      return imageAuthorView;
    }

    public TextView getTextTitleView() {
      return textTitleView;
    }

  }

  private class AuthorAdapterDataObserver extends RecyclerView.AdapterDataObserver {
    @Override
    public void onChanged() {
      updateAuthorItemList();
    }
  }

}
