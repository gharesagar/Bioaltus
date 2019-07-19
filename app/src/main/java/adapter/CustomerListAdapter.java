package adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.bio.bioaltus.R;

import java.util.List;

import model.FetchCustomerByEMP;

public class CustomerListAdapter extends RecyclerView.Adapter<CustomerListAdapter.ViewHolder>{
    List<FetchCustomerByEMP> customerList;

    public CustomerListAdapter(List<FetchCustomerByEMP> customerList) {
        this.customerList = customerList;
    }

    @NonNull
    @Override
    public CustomerListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_data_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerListAdapter.ViewHolder viewHolder, int i) {
        FetchCustomerByEMP fetchCustomerByEMP=customerList.get(i);
        viewHolder.tvLocation.setText(fetchCustomerByEMP.getLocation());
        viewHolder.tvCheckInTime.setText(fetchCustomerByEMP.getCheckInTime());
        viewHolder.tvchkOut.setText(fetchCustomerByEMP.getCheckoutTime());
        viewHolder.tvCardName.setText(fetchCustomerByEMP.getCardName());
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCardName,tvCheckInTime,tvchkOut,tvLocation;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCardName=itemView.findViewById(R.id.tvCardName);
            tvCheckInTime=itemView.findViewById(R.id.tvCheckinTime);
            tvchkOut=itemView.findViewById(R.id.tvCheckoutTime);
            tvLocation=itemView.findViewById(R.id.tvLocation);
        }
    }
}
