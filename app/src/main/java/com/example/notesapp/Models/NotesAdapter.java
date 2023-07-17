package com.example.notesapp.Models;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.R;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {
    ArrayList<NotesModel> notes;
    Context context;
    OnItemClickListener listener;
    OnItemLongClickListener onItemLongClickListener;
    public interface OnItemClickListener {
        void onItemClick(int position,View view);
    }
    public interface OnItemLongClickListener {
        void onItemLongClick(int position,View view);
    }
    public NotesAdapter(Context context, ArrayList<NotesModel> notes, OnItemClickListener listener,
                        OnItemLongClickListener onItemLongClickListener) {
        this.notes = notes;
        this.context = context;
        this.listener = listener;
        this.onItemLongClickListener = onItemLongClickListener;
    }
    public int getAdapterPosition(int position) {
        return position;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_notes_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NotesModel model = notes.get(position);
        holder.title.setText(model.title);
        // Set the content text
        holder.content.setText(model.content);

        // Truncate the text if exceeding three lines
        holder.content.post(new Runnable() {
            @Override
            public void run() {
                int lineCount = holder.content.getLineCount();
                int maxLines = 3;

                if (lineCount > maxLines) {
                    int lineEndIndex = holder.content.getLayout().getLineEnd(maxLines - 1);
                    String trimmedText = model.content.substring(0, lineEndIndex);
                    trimmedText = trimmedText.replaceAll("\\s+$", "")+"...";
                    holder.content.setText(trimmedText);
                }
            }
        });

        // applying random colors to item

        holder.bind(position,holder.itemView);
        holder.click(position,holder.itemView);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
      TextView title;
      TextView content;
      CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notetitle);
            content = itemView.findViewById(R.id.notes_content);
            cardView = itemView.findViewById(R.id.card_view);
        }

        public void bind(int position,View view){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(position, view);
                }
            });
        }
        public void click(int position,View view){
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    onItemLongClickListener.onItemLongClick(position,view);
                    return true;
                }
            });
        }
    }

    private String trimTextToLimit(String text, int maxLines, int maxCharacters) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }

        String[] lines = text.split("\n");
        StringBuilder trimmedText = new StringBuilder();
        int lineCount = 0;

        for (String line : lines) {
            if (lineCount >= maxLines) {
                break;
            }

            if (line.length() > maxCharacters) {
                line = line.substring(0, maxCharacters) + "...";
            }

            trimmedText.append(line).append("\n");
            lineCount++;
        }

        return trimmedText.toString().trim();
    }

}
