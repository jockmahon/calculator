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

	List<String> e;
	List<String> k;
	
	EquationInterface equationInter;
	
	public interface EquationInterface
	{
		public void onDialogPositiveClick( DialogFragment dialog, String equation );
		public void onDialogNegativeClick( DialogFragment dialog );

	}


	@Override
	public Dialog onCreateDialog( Bundle savedState )
	{
		AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );

		builder.setTitle( getString( R.string.menu_saved_equations ) );

		SharedPreferences sp = getActivity().getPreferences( getActivity().MODE_PRIVATE );

		Map<String, ?> equ = (Map<String, ?>) sp.getAll();

		e = new ArrayList<String>();
		k = new ArrayList<String>();
		for(String key : equ.keySet())
		{
			e.add( (String) equ.get( key ) );
			k.add( key );
		}

		String[] q = e.toArray( new String[e.size()] );

		builder.setSingleChoiceItems( q, 0, this );

		builder.setPositiveButton( "Open", this );
		builder.setNegativeButton( "Delete", this );

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
		Log.d( "MOOSE", String.valueOf( id ) );

		ListView lw = ((AlertDialog)dialog).getListView();
		String rtn = (String) lw.getAdapter().getItem(lw.getCheckedItemPosition());
		
		if( id == -1 )
		{
			
			equationInter.onDialogPositiveClick( SavedEquationsDialog.this, rtn );	
		}
		else if( id == -2)
		{
			SharedPreferences sp = getActivity().getPreferences( getActivity().MODE_PRIVATE );
			SharedPreferences.Editor editor = sp.edit();
			
			editor.remove( k.get(lw.getCheckedItemPosition()  ) );
			editor.commit();
			
		}
			
	}

}
