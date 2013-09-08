package com.jock.calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.ListView;

public class SavedEquationsDialog extends DialogFragment implements DialogInterface.OnClickListener
{
	public static final String NO_SAVED_EQUATIONS = "-1";

	List<String> keyList;
	List<String> valueList;

	Boolean isSavedEquations;

	EquationInterface equationInter;

	public interface EquationInterface
	{
		public void onDialogPositiveClick( DialogFragment dialog, String equation );


		public void onDialogNegativeClick( DialogFragment dialog, String equation );

	}


	@Override
	public Dialog onCreateDialog( Bundle savedState )
	{

		SharedPreferences sp = getActivity().getPreferences( getActivity().MODE_PRIVATE );

		Map<String, ?> savedEqu = (Map<String, ?>) sp.getAll();

		keyList = new ArrayList<String>();
		valueList = new ArrayList<String>();
		for(String key : savedEqu.keySet())
		{
			valueList.add( (String) savedEqu.get( key ) );
			keyList.add( key );
		}

		String[] valueArrray = valueList.toArray( new String[valueList.size()] );

		AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
		builder.setTitle( getString( R.string.menu_saved_equations ) );

		if( valueArrray.length > 0 )
		{
			isSavedEquations = true;
			builder.setSingleChoiceItems( valueArrray, 0, this );

			builder.setPositiveButton( "Open", this );
			builder.setNegativeButton( "Delete", this );
		}
		else
		{
			isSavedEquations = false;
			builder.setMessage( getString( R.string.msg_no_saved_equ ) );
			builder.setPositiveButton( "Go on then", this );
			builder.setNegativeButton( "Not now", this );
		}
		return builder.create();
	}


	@Override
	public void onAttach( Activity activity )
	{
		super.onAttach( activity );
		try
		{
			equationInter = (EquationInterface) activity;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException( activity.toString() + " must implement EquationInterface" );
		}
	}


	public void onClick( DialogInterface dialog, int id )
	{
		if( isSavedEquations )
		{
			ListView lw = ( (AlertDialog) dialog ).getListView();
			String rtn = (String) lw.getAdapter().getItem( lw.getCheckedItemPosition() );
			if( id == -1 )
			{
				equationInter.onDialogPositiveClick( SavedEquationsDialog.this, rtn );
			}
			else if( id == -2 )
			{
				SharedPreferences sp = getActivity().getPreferences( getActivity().MODE_PRIVATE );
				SharedPreferences.Editor editor = sp.edit();

				editor.remove( keyList.get( lw.getCheckedItemPosition() ) );
				editor.commit();

				equationInter.onDialogNegativeClick( SavedEquationsDialog.this, rtn );
			}
		}
		else
		{
			if( id == -1 )
			{
				equationInter.onDialogPositiveClick( SavedEquationsDialog.this, NO_SAVED_EQUATIONS );
			}
			else if( id == -2 )
			{
				equationInter.onDialogNegativeClick( SavedEquationsDialog.this ,NO_SAVED_EQUATIONS);
			}
		}
	}
}
