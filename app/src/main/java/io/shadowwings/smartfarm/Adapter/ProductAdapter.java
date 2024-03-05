package io.shadowwings.smartfarm.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.shadowwings.smartfarm.Model.CartItemModel;
import io.shadowwings.smartfarm.Model.ProductModel;
import io.shadowwings.smartfarm.ProductListClickListener;
import io.shadowwings.smartfarm.R;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.MyView> {

    private final List<ProductModel> list;
    ProductListClickListener productListClickListener;
    Activity ACTIVITY;

    public class MyView extends RecyclerView.ViewHolder {

        TextView TXT_NAME;
        TextView brandName,offer;
        TextView TXT_PRICE, TXT_DEAL_PRICE;
        ImageView PRODUCT_IMG;
        Button ADD_TO_CART;

        public MyView(View view) {
            super(view);
            TXT_NAME = view.findViewById(R.id.item_brand_name);
            brandName=view.findViewById(R.id.cart_brandName);
            TXT_PRICE = view.findViewById(R.id.cart_item_mrp);
            TXT_DEAL_PRICE = view.findViewById(R.id.cart_item_price);
            PRODUCT_IMG = view.findViewById(R.id.cart_Item_img);
            offer=view.findViewById(R.id.cart_item_offer);
            ADD_TO_CART=view.findViewById(R.id.addToCart);
        }
    }

    public ProductAdapter(List<ProductModel> horizontalList, Activity activity) {
        this.productListClickListener = productListClickListener;
        this.list = horizontalList;
        ACTIVITY = activity;
    }

    @NonNull
    @Override
    public MyView onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.farma_item, parent, false);
        return new MyView(itemView);
    }

    @Override
    public void onBindViewHolder(final MyView holder, @SuppressLint("RecyclerView") final int position) {
        ProductModel model = list.get(position);

        holder.TXT_NAME.setText(model.NAME);
        holder.brandName.setText(model.brandName);
        holder.TXT_DEAL_PRICE.setText(model.DEAL_PRICE + " Rs. ");
        holder.TXT_PRICE.setText(" MRP: " + model.PRICE + " Rs. ");
        holder.TXT_PRICE.setPaintFlags(holder.TXT_PRICE.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        Picasso.get().load(model.URL).into(holder.PRODUCT_IMG);

        holder.ADD_TO_CART.setOnClickListener(v -> {
            String samount = model.DEAL_PRICE;
            int amount = Math.round(Float.parseFloat(samount) * 100);
            Checkout checkout = new Checkout();
            checkout.setKeyID("rzp_test_UuUl6z7PAbEpHS");
            checkout.setImage(R.drawable.app_logo);
            JSONObject object = new JSONObject();
            try {
                object.put("name", "Smart Farm");
                object.put("description", model.NAME);
                object.put("theme.color", "#018241");
                object.put("currency", "INR");
                object.put("amount", amount);
                object.put("prefill.contact", "7203813075");
                object.put("prefill.email", "mahedialivijapura@gmail.com");
                checkout.open(ACTIVITY, object);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(ACTIVITY, "Unknown Error Occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToCart(ProductModel productModel) {
        DatabaseReference useCart= FirebaseDatabase.getInstance().getReference("Cart").child("UNIQUE_USER_ID");
        useCart.child(productModel.getKEY()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot cartSnapShot:snapshot.getChildren())
                {
                    CartItemModel cartModel=cartSnapShot.getValue(CartItemModel.class);
                    cartModel.setKEY(cartSnapShot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

