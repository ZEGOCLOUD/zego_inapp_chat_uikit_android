//package com.zegocloud.zimkit.components.message.widget.input;
//
//import android.content.Context;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.util.AttributeSet;
//import android.view.GestureDetector.SimpleOnGestureListener;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.LinearLayout;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.view.GestureDetectorCompat;
//import androidx.databinding.DataBindingUtil;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.recyclerview.widget.RecyclerView.SimpleOnItemTouchListener;
//import com.zegocloud.zimkit.R;
//import com.zegocloud.zimkit.components.message.ui.TestGroup;
//import com.zegocloud.zimkit.components.message.widget.MessageRecyclerView;
//import com.zegocloud.zimkit.databinding.ZimkitLayoutInputViewBinding;
//
//
//public class ZIMKitInputView3 extends LinearLayout {
//
//    private ZimkitLayoutInputViewBinding binding;
//    private GestureDetectorCompat gestureDetectorCompat;
//    private MessageRecyclerView recyclerView;
//
//
//    public ZIMKitInputView3(Context context) {
//        super(context);
//        initView(context);
//    }
//
//    public ZIMKitInputView3(Context context, @Nullable AttributeSet attrs) {
//        super(context, attrs);
//        initView(context);
//    }
//
//    public ZIMKitInputView3(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        initView(context);
//    }
//
//    private static final String TAG = "ZIMKitInputView3";
//
//    private void initView(Context context) {
//        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.zimkit_layout_input_view, this, true);
//        //        binding.inputContainer.setVisibility(VISIBLE);
//
//        gestureDetectorCompat = new GestureDetectorCompat(getContext(), new SimpleOnGestureListener() {
//
//            @Override
//            public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX,
//                float distanceY) {
//                TestGroup parent = (TestGroup) getParent();
//                parent.smoothScrollTo(0, 0);
//                unselectAllButtons();
//                hideInputWindow(binding.inputEdittext);
//                return true;
//            }
//        });
//
//        binding.inputAudio.setOnClickListener(v -> {
//            boolean selected = v.isSelected();
//            boolean newState = !selected;
//            v.setSelected(newState);
//            if (newState) {
//
//            } else {
//
//            }
//        });
//
//        binding.inputEmoji.setOnClickListener(v -> {
//            boolean selected = v.isSelected();
//            boolean newState = !selected;
//            v.setSelected(newState);
//            if (newState) {
//                hideInputWindow(binding.inputEdittext);
//                TestGroup parent = (TestGroup) getParent();
//                //                parent.smoothScrollTo(0, binding.inputContainer.getHeight());
//            } else {
//                requestInputWindow(binding.inputEdittext);
//            }
//        });
//
//        binding.inputEdittext.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    unselectAllButtons();
//                    requestInputWindow(view);
//                    TestGroup parent = (TestGroup) getParent();
//                    //                    parent.smoothScrollTo(0, binding.inputContainer.getHeight());
//                }
//                return false;
//            }
//        });
//        binding.inputSend.setEnabled(false);
//        binding.inputEdittext.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                binding.inputSend.setEnabled(s.length() > 0);
//            }
//        });
//    }
//
//    private void unselectAllButtons() {
//        binding.inputAudio.setSelected(false);
//        binding.inputEmoji.setSelected(false);
//        binding.inputPic.setSelected(false);
//        binding.inputMore.setSelected(false);
//    }
//
//    public void attachToRecyclerView(MessageRecyclerView recyclerView) {
//        this.recyclerView = recyclerView;
//        recyclerView.addOnItemTouchListener(new SimpleOnItemTouchListener() {
//            @Override
//            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
//                gestureDetectorCompat.onTouchEvent(e);
//                return super.onInterceptTouchEvent(rv, e);
//            }
//        });
//    }
//
//    public static void hideInputWindow(View view) {
//        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//        view.clearFocus();
//    }
//
//    public static void requestInputWindow(View view) {
//        view.requestFocus();
//        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//        boolean input = false;
//        if (view.isAttachedToWindow()) {
//            input = imm.showSoftInput(view, 0);
//        }
//    }
//}
