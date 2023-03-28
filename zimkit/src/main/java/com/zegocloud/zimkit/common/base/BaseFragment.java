package com.zegocloud.zimkit.common.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.lang.reflect.ParameterizedType;

public abstract class BaseFragment<T extends ViewDataBinding, VM extends ViewModel> extends Fragment {

    protected T mBinding;
    protected VM mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        if (getViewModelId() != 0) {
            Class<VM> entityClass = (Class<VM>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
            mViewModel = new ViewModelProvider(requireActivity()).get(entityClass);
            mBinding.setVariable(getViewModelId(), mViewModel);
        }
        mBinding.setLifecycleOwner(this);
        initView();
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }

    protected abstract void initView();

    protected abstract int getLayoutId();

    protected abstract int getViewModelId();

    protected abstract void initData();

}
