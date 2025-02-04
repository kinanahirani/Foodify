package com.example.campus_services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PlaceOrder extends AppCompatActivity implements View.OnClickListener{

    private String CanteenName,OrderString,customerId,canteenID, userID, user, InstructionString;
    private Button PlaceOrderB,changePaymentMethod;
    private TextView amt,currentBalance,paymentMethod;
    private ListView CookingInstruction;
    private ListView disp;
    private int amount=0;
    private int flag=0;
    private Order OrderNew;
    private ArrayList<String> mItemName,mI;
    private ArrayAdapter<String> arrayAdapter,mAdapter;
    private String availableBalance, currentPaymentMethod = "0";

    private ValueEventListener newlistner;
    private DatabaseReference db4;
    private FirebaseAuth mAuth,firebaseAuth;

    private ArrayList<String> instr;
    private ArrayList<ArrayList<String>> instructions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null)
        {
            startActivity(new Intent(getApplicationContext(),Login_Activity.class));
            finish();
        }
        customerId = mAuth.getCurrentUser().getEmail();

        final Intent intent  = getIntent();
        CanteenName = intent.getStringExtra("CanteenName");
        OrderString = intent.getStringExtra("OrderString");
        InstructionString = intent.getStringExtra("InstructionString");
        instr = intent.getStringArrayListExtra("CookI");

        FirebaseDatabase fbd = FirebaseDatabase.getInstance();
        fbd.getReference("Users/Canteen").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Canteen item = ds.getValue(Canteen.class);
                    if(item.getName().equals(CanteenName)){
                        canteenID = ds.getKey();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        PlaceOrderB = findViewById(R.id.btnPlaceOrderFinal);
        changePaymentMethod = findViewById(R.id.btnChangePaymentType);
        amt = findViewById(R.id.tvTotalAmount);
        currentBalance = findViewById(R.id.tvPlaceOrderCurrentBalance);
       // paymentMethod = findViewById(R.id.tvPaymentType);
        disp = findViewById(R.id.lvPlaceOrderList);
        //CookingInstruction = findViewById(R.id.lvCookingInstr);
        mItemName= new ArrayList<>();
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.dish_info,R.id.dishnameid,instr);

        mI= new ArrayList<>();
        mAdapter = new ArrayAdapter<String>(this, R.layout.dish_info,R.id.dishnameid,mI);
        instructions = new ArrayList<ArrayList<String>>();

        userID = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if(userID.charAt(0)>='0' && userID.charAt(0)<='9'){
            user="Student";
        }
        else{
            user="Professor";
        }
        int it=0;
        while(userID.charAt(it) != '@') {
            it++;
        }
        if(user.equals("Professor")){
            userID = mAuth.getCurrentUser().getUid();
        }
        else {
            userID = userID.substring(0, it);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user2 = firebaseAuth.getCurrentUser();
        final String userid = userID;
        final FirebaseDatabase db3 = FirebaseDatabase.getInstance();
        db4 = db3.getReference("Users/"+user);
        newlistner =  db4.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User usertemp = dataSnapshot.child(userid).getValue(User.class);
                availableBalance = usertemp.getVirtual_Money();
                currentBalance.setText("₹" + availableBalance);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (OrderString.length() == 0){
            Intent intent1 = new Intent(getApplicationContext(), CanteenMenu.class);
            intent1.putExtra("OrderString", OrderString);
            intent1.putExtra("CanteenName",CanteenName);
            intent1.putExtra("InstructionString",InstructionString);
            intent1.putExtra("CookI",instr);
            finish();
            startActivity(intent1);
        }
        else {
            int c = 0, pre = 0;
            String temp = "";

            for (int i = 0; i < OrderString.length(); i++) {
                if (OrderString.charAt(i) == '\n') {
                    c++;
                    if (c % 3 == 2) {
                        temp = OrderString.substring(pre, i);
                        pre = i + 1;
                    } else if (c % 3 == 0) {
                        temp = temp + "\nQuantity: " + OrderString.substring(pre, i);
                        pre = i + 1;
                        mItemName.add(temp);
                    }
                }
            }
            temp += "\nQuantity: " + OrderString.substring(pre, OrderString.length());
            mItemName.add(temp);
            disp.setAdapter(arrayAdapter);

            for (int i = 0; i < mItemName.size(); i++) {
                int pree = 0, flg = 0;
                String amou = "", quty = "";
                for (int j = 0; j < mItemName.get(i).length(); j++) {
                    if (flg == 1 && mItemName.get(i).charAt(j) == '\n') {
                        amou = mItemName.get(i).substring(pree, j);
                    }
                    if (flg == 0 && mItemName.get(i).charAt(j) >= '0' && mItemName.get(i).charAt(j) <= '9') {
                        flg = 1;
                        pree = j;
                    }
                }
                flg = 0;
                for (int j = 0; j < mItemName.get(i).length(); j++) {
                    if (mItemName.get(i).charAt(j) == '\n') {
                        flg++;
                    }
                    if (flg == 2 && mItemName.get(i).charAt(j) >= '0' && mItemName.get(i).charAt(j) <= '9') {
                        quty = mItemName.get(i).substring(j, mItemName.get(i).length());
                        break;
                    }
                }
                Log.v("PlaceOrder", "---------------" + amou + "-----------" + quty);
                amount += Integer.parseInt(amou) * Integer.parseInt(quty);
            }

            c = 0;pre = 0;
            temp = "";

            for (int i = 0; i < InstructionString.length(); i++) {
                if (InstructionString.charAt(i) == '\n') {
                    c++;
                    if (c % 3 == 2) {
                        temp = InstructionString.substring(pre, i);
                        pre = i + 1;
                    } else if (c % 3 == 0) {
                        temp = temp + InstructionString.substring(pre, i);
                        pre = i + 1;
                        mI.add(temp);
                    }
                }
            }
            temp += InstructionString.substring(pre, InstructionString.length());
            mI.add(temp);
            //CookingInstruction.setAdapter(mAdapter);

            final FirebaseDatabase db = FirebaseDatabase.getInstance();
            db.getReference("CanteenMenu/" + CanteenName).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                        Item item = ds.getValue(Item.class);
                        instructions.add(item.getInstructions());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            disp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String temp = "";
                    int c = 0;
                    for (int i = 0; i < mItemName.get(position).length(); i++) {
                        if (mItemName.get(position).charAt(i) == '\n') {
                            c++;
                        }
                        if (c == 2) {
                            temp = mItemName.get(position).substring(0, i);
                            break;
                        }
                    }
                    Intent intent1 = new Intent(getApplicationContext(), ItemQuantity.class);
                    intent1.putExtra("CurrentActivity", "PlaceOrder");
                    intent1.putExtra("CurrentDish", temp);
                    intent1.putExtra("OrderString", OrderString);
                    intent1.putExtra("CanteenName", CanteenName);
                    intent1.putExtra("InstructionString", InstructionString);
                    intent1.putExtra("CookingInstructions",instructions.get(position));
                    intent1.putExtra("CookI",instr);
                    finish();
                    startActivity(intent1);
                }
            });
            amt.setText(Integer.toString(amount));
            PlaceOrderB.setOnClickListener(this);
            changePaymentMethod.setOnClickListener(this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        db4.removeEventListener(newlistner);
        Intent intent1 = new Intent(getApplicationContext(), CanteenMenu.class);
        intent1.putExtra("OrderString", OrderString);
        intent1.putExtra("CanteenName",CanteenName);
        intent1.putExtra("InstructionString",InstructionString);
        intent1.putExtra("CookI",instr);
        finish();
        startActivity(intent1);

    }

    @Override
    public void onClick(View v) {
        if (v == changePaymentMethod){
            if (currentPaymentMethod.equals("0")) {
                if (amount > Integer.parseInt(availableBalance)) {
                    Toast.makeText(getApplicationContext(), "Not Enough Balance", Toast.LENGTH_SHORT).show();
                } else {
                    currentPaymentMethod = "1";
                    paymentMethod.setText("Online");
                }
            }
            else {
                currentPaymentMethod = "0";
                paymentMethod.setText("Cash");
            }
        }

        if (v == PlaceOrderB){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
            final String currentDateandTime = sdf.format(new Date());
            //Toast.makeText(getApplicationContext(),currentDateandTime,Toast.LENGTH_LONG).show();

            final FirebaseDatabase dbPlace = FirebaseDatabase.getInstance();
            final DatabaseReference dbRef = dbPlace.getReference("CanteenOrderCount");
            dbRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    String orderNo1 = dataSnapshot.getValue(String.class);
                    OrderNew = new Order(orderNo1, customerId, CanteenName, OrderString, currentDateandTime.toString(), instr, currentPaymentMethod,"Cooking");
                    final FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                    final DatabaseReference table_order = database1.getReference("CurrentOrders");
                    table_order.child(OrderNew.getOrderNo()).setValue(OrderNew);
                    final FirebaseDatabase db9 = FirebaseDatabase.getInstance();
                    final DatabaseReference database9 = db9.getReference("CanteenOrderCount");
                    database9.child("Count").setValue(Integer.toString(Integer.parseInt(OrderNew.getOrderNo()) + 1));
                    //Toast.makeText(getApplicationContext(),OrderNew.getOrderNo(),Toast.LENGTH_LONG).show();

                    if (currentPaymentMethod.equals("1")){
                        final FirebaseDatabase dab = FirebaseDatabase.getInstance();
                        final DatabaseReference tab = dab.getReference("Users/"+ user + "/" + customerId);
                        tab.child("virtual_Money").setValue(Integer.toString(Integer.parseInt(availableBalance)-amount));
                        final DatabaseReference lab = dab.getReference("Users/Canteen/" + canteenID);
                        lab.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                String canteenAmount = dataSnapshot1.child("Virtual_Money").getValue(String.class);
                                lab.removeEventListener(this);
                                final DatabaseReference fab = dab.getReference("Users/Canteen/" + canteenID);
                                fab.child("Virtual_Money").setValue(Integer.toString(Integer.parseInt(canteenAmount) - amount));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    dbRef.removeEventListener(this);
                    db4.removeEventListener(newlistner);
                    Toast.makeText(getApplicationContext(), "Amount : "+amount, Toast.LENGTH_SHORT).show();
                    Intent intentNew = new Intent(PlaceOrder.this, Payment.class);
                    intentNew.putExtra("OrderNo", OrderNew.getOrderNo());
                    intentNew.putExtra("amount", amount);
                    startActivity(intentNew);
                    finish();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

}
