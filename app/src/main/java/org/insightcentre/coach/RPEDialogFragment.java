package org.insightcentre.coach;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

/**
 * Activities that contain this fragment must implement the
 * {@link RPEDialogFragment.RPEDialogListener} interface
 * to handle interaction events.
 * Use the {@link RPEDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RPEDialogFragment extends DialogFragment {
    private static final String ARG_CHECKED_RPE = "rpe";

    private int mRPE = Utility.DEFAULT_RPE;

    private RPEDialogListener mListener;

    public RPEDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameter.
     *
     * @param checkedRPE checked RPE
     * @return A new instance of fragment RPEDialogFragment.
     */
    public static RPEDialogFragment newInstance(int checkedRPE) {
        RPEDialogFragment fragment = new RPEDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CHECKED_RPE, checkedRPE);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRPE = getArguments().getInt(ARG_CHECKED_RPE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCustomTitle(getActivity().getLayoutInflater().inflate(R.layout.custom_title_choose_rpe_dialog, null));
        builder.setSingleChoiceItems(R.array.rpe_values, mRPE - Utility.RPE_OFFSET, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mRPE = i + Utility.RPE_OFFSET;
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mListener.onChooseRPE(mRPE);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mListener.onCancelChoosingRPE();
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (RPEDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ChooseRPEDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface RPEDialogListener {
        void onChooseRPE(int RPE);
        void onCancelChoosingRPE();
    }
}
