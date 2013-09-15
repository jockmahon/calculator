package com.jock.calculator;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements OnSharedPreferenceChangeListener, SavedEquationsDialog.EquationInterface
{
	private static final String APP_TAG = "MY_CAL";
	private static final String STATE_INPUTS = "inputs";
	private static final String STATE_HISTORY = "history";

	ArrayList<String> inputs = new ArrayList<String>();
	ArrayList<String> history = new ArrayList<String>();

	private EditText et_cal;
	private Button btn_clr;
	private Button btn_equals;
	private Button btn_plus;
	private Button btn_mins;
	private Button btn_mult;
	private Button btn_divide;
	private Button btn_leftBraket;
	private Button btn_rightBraket;
	private Button btn_question;
	private Boolean isEquationMode;
	private int replaceIndex;

	private SharedPreferences prefs;


	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		prefs = PreferenceManager.getDefaultSharedPreferences( this );
		prefs.registerOnSharedPreferenceChangeListener( this );

		Boolean isReverse = prefs.getBoolean( PreferencesActivity.KEY_PREF_REVERSE_NUMBERS, false );

		setLayout( isReverse );

		// if there is a saved state restore it
		if( savedInstanceState != null )
		{
			inputs = savedInstanceState.getStringArrayList( STATE_INPUTS );
			history = savedInstanceState.getStringArrayList( STATE_HISTORY );
		}
		else
		{
			inputs.add( "" );
		}

		buttonSetUp();

		setIsEquationMode( false );
		replaceIndex = -1;
	}


	private void buttonSetUp()
	{
		btn_plus = (Button) findViewById( R.id.plus_btn );
		btn_mins = (Button) findViewById( R.id.minus_btn );
		btn_mult = (Button) findViewById( R.id.mult_btn );
		btn_divide = (Button) findViewById( R.id.divide_btn );
		btn_leftBraket = (Button) findViewById( R.id.leftBracket_btn );
		btn_rightBraket = (Button) findViewById( R.id.rightBracket_btn );
		btn_clr = (Button) findViewById( R.id.clr_btn );
		btn_question = (Button) findViewById( R.id.question_btn );

		btn_clr.setOnLongClickListener( new OnLongClickListener()
		{
			@Override
			public boolean onLongClick( View v )
			{
				reset();
				return false;
			}
		} );

	}


	@Override
	protected void onPause()
	{
		super.onPause();
	}


	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		prefs.unregisterOnSharedPreferenceChangeListener( this );
	}


	@Override
	protected void onSaveInstanceState( Bundle outState )
	{
		outState.putStringArrayList( STATE_INPUTS, inputs );
		outState.putStringArrayList( STATE_HISTORY, history );

		super.onSaveInstanceState( outState );
	}


	public void onSharedPreferenceChanged( SharedPreferences prefs, String key )
	{
		if( key.equals( PreferencesActivity.KEY_PREF_REVERSE_NUMBERS ) )
		{
			setLayout( prefs.getBoolean( PreferencesActivity.KEY_PREF_REVERSE_NUMBERS, false ) );
		}
	}


	private void setLayout( Boolean isReverse )
	{
		if( isReverse )
		{
			setContentView( R.layout.layout_main_reverse );
		}
		else
		{
			setContentView( R.layout.layout_main );
		}

		btn_equals = (Button) findViewById( R.id.equals_btn );
		et_cal = (EditText) findViewById( R.id.editText );
		btn_question = (Button) findViewById( R.id.question_btn );

		setCalText();

	}


	// helper function to remove elements from an ArrayList
	private ArrayList<String> remove( ArrayList<String> list, int from, int to )
	{
		for(int i = to; i > from; i--)
		{
			list.remove( i - 1 );
		}
		return list;
	}


	// calculate the contents of any brackets
	private ArrayList<String> bracketContents( ArrayList<String> inputStrings ) throws ArrayIndexOutOfBoundsException
	{
		boolean cont = true;
		int openBracketPos = -1;

		while (cont)
		{

			Log.d( APP_TAG, "bracket" );
			int closeBracketPos = inputStrings.size();

			// get the last open bracket and work forward to get its matching
			// close bracket
			openBracketPos = inputStrings.lastIndexOf( "(" );
			if( openBracketPos != -1 )
			{
				for(int i = openBracketPos; i < inputStrings.size() - 1; i++)
				{
					if( ( inputStrings.get( i ).equals( ")" ) ) )
					{
						closeBracketPos = i + 1;
						break;
					}
				}

				// extract the bracket contents including the brackets
				ArrayList<String> bracketContent = new ArrayList<String>( inputStrings.subList( openBracketPos, closeBracketPos ) );

				// remove the bracket content which will be replaced with its
				// result
				inputs = remove( inputs, openBracketPos, closeBracketPos );

				// remove the (
				bracketContent.remove( 0 );

				if( bracketContent.size() > 0 )
				{
					// if the last element was ) then remove it
					if( bracketContent.get( bracketContent.size() - 1 ).equals( ")" ) )
					{
						bracketContent.remove( bracketContent.size() - 1 );
					}

					if( bracketContent.size() > 0 )
					{
						// calculate the bracket equation
						bracketContent = operatorPrecedence( bracketContent );
						bracketContent = calculate( bracketContent );

						// if there is o oparator before a bracket default to *
						if( ( openBracketPos > 0 ) && ( isOperator( inputs.get( openBracketPos - 1 ) ) ) )
						{
							// put the result back in the main inputs ArrayList
							inputs.add( openBracketPos, bracketContent.get( 0 ) );
						}
						else
						{
							inputs.add( openBracketPos, bracketContent.get( 0 ) );
							if( openBracketPos > 0 )
							{
								inputs.add( openBracketPos, "*" );
							}
						}
					}
				}
			}
			else
			{
				// if there is no (, above, and no ) the exit the while else add
				// a ( to complete the set
				if( inputStrings.lastIndexOf( ")" ) == -1 )
				{
					cont = false;
				}
				else
				{
					inputs.add( 0, "(" );
				}
			}

		}

		return inputs;
	}


	private ArrayList<String> calculate( ArrayList<String> equation )
	{
		Boolean cont = true;

		while (cont)
		{
			Log.d( APP_TAG, "calculate" );
			// if only one value the exit
			if( equation.size() == 1 )
			{
				cont = false;
			}
			else
			{
				// do the math , * and / will already be done
				if( equation.size() > 2 )
				{
					Float value1 = Float.parseFloat( equation.get( 0 ) );
					Float value2 = Float.parseFloat( equation.get( 2 ) );

					Float calculation = 0.0f;

					if( equation.get( 1 ).equals( "+" ) )
					{
						calculation = value1 + value2;
					}
					else if( equation.get( 1 ).equals( "-" ) )
					{
						calculation = value1 - value2;
					}

					equation.remove( 0 );
					equation.remove( 0 );
					equation.set( 0, String.valueOf( calculation ) );
				}
				else
				{
					equation.remove( 1 );
					cont = false;
				}
			}
		}
		return equation;
	}


	// do all the * and / first
	private ArrayList<String> operatorPrecedence( ArrayList<String> inputStrings )
	{
		Boolean cont = true;
		while (cont)
		{
			Log.d( APP_TAG, "operatorPrecedence" );
			for(int i = 0; i < inputStrings.size() - 1; i++)
			{
				if( ( inputStrings.get( i ).equals( "*" ) ) || ( inputStrings.get( i ).equals( "/" ) ) )
				{
					Float left = Float.parseFloat( inputStrings.get( i - 1 ) );
					Float right = Float.parseFloat( inputStrings.get( i + 1 ) );

					Float value = 0.0f;

					if( inputStrings.get( i ).equals( "*" ) )
					{
						value = left * right;
					}
					else if( inputStrings.get( i ).equals( "/" ) )
					{
						value = left / right;
					}

					inputStrings.set( i - 1, String.valueOf( value ) );
					inputStrings.remove( i );
					inputStrings.remove( i );
				}
			}

			cont = ( inputStrings.contains( "*" ) || inputStrings.contains( "/" ) );
		}

		return inputStrings;
	}


	// when the = is pressed
	public void onEqualsClick( View v )
	{
		String equationText = et_cal.getText().toString();

		if( btn_equals.getText().equals( "Save" ) )
		{
			SharedPreferences sp = this.getPreferences( Context.MODE_PRIVATE );
			SharedPreferences.Editor editor = sp.edit();

			Random rand = new Random();

			int randomNum = rand.nextInt( 1000 );

			editor.putString( "EQUATION_KEY_" + String.valueOf( randomNum ), equationText );
			editor.commit();

			Toast toast = Toast.makeText( this, getString( R.string.toast_equation_saved ), Toast.LENGTH_SHORT );
			toast.show();

			toggleEquationModeLayout( false );

			reset();
		}
		else
		{

			if( equationText.contains( "?" ) )
			{
				Toast toast = Toast.makeText( this, getString( R.string.toast_error_msg ), Toast.LENGTH_SHORT );
				toast.show();
				return;
			}

			if( !equationText.equals( "" ) )
			{
				Boolean cont = true;

				// add the equation to the history
				addHistory( et_cal.getText().toString() );

				checkForOrphanOp();

				inputs = bracketContents( inputs ); // calculate the contents of all ()
				inputs = operatorPrecedence( inputs ); // calculate all the * and /

				while (cont)
				{
					Log.d( APP_TAG, "onEquals" );
					if( inputs.size() <= 1 )
					{
						cont = false;
					}
					else if( inputs.size() == 2 )
					{
						if( isOperator( inputs.get( 0 ) ) )
						{
							inputs.remove( 0 );
						}
						else
						{
							inputs.remove( 1 );
						}

					}
					else
					{

						if( inputs.size() > 2 )
						{
							Float value1 = Float.parseFloat( inputs.get( 0 ) );
							Float value2 = Float.parseFloat( inputs.get( 2 ) );

							Float calculation = 0.0f;

							if( inputs.get( 1 ).equals( "+" ) )
							{
								calculation = value1 + value2;
							}
							else if( inputs.get( 1 ).equals( "-" ) )
							{
								calculation = value1 - value2;
							}

							inputs.remove( 0 );
							inputs.remove( 0 );
							inputs.set( 0, String.valueOf( calculation ) );
						}
					}
				}

				// fix for issu where they only enter a single operator ie "+"
				if( inputs.size() >= 1 )
				{
					et_cal.setText( checkRemainder( inputs.get( 0 ) ) );
				}
				else
				{
					et_cal.setText( "" );
					inputs.add( "" );
				}

				replaceIndex = -1;
				setIsEquationMode( false );
			}
		}
	}


	// if the first or last input is a operator than insert dummy value so the
	// calculation can work
	private void checkForOrphanOp()
	{
		if( isOperator( inputs.get( 0 ) ) )
		{
			inputs.remove( 0 );
		}
		else if( isOperator( inputs.get( inputs.size() - 1 ) ) )
		{
			inputs.remove( inputs.size() - 1 );
			// if( inputs.get( inputs.size() - 1 ).equals( "/" ) )
			// {
			// inputs.add( inputs.size(), "1" );
			// }
			// else
			// {
			// inputs.add( inputs.size(), "0" );
			// }
		}
	}


	// used to remove tailing zero on the number ie float values are 4.0, if
	// remainder is 0 then return only the 4
	private String checkRemainder( String value )
	{
		if( value.equals( "" ) )
		{
			return "";
		}
		else if( ( isOperator( value ) ) || ( value.lastIndexOf( "." ) == value.length() - 1 ) || ( isBracket( value ) ) || value.equals( "?" ) )
		{
			return value;
		}
		else
		{
			float a = Float.parseFloat( value );
			String[] arr = Float.toString( a ).split( "\\." );

			if( arr[1].equals( "0" ) )
			{
				return String.valueOf( arr[0] );
			}
			else
			{
				return String.valueOf( value );
			}
		}
	}


	public void onQuestionClick( View v )
	{
		Button but = (Button) findViewById( v.getId() );
		inputs.add( String.valueOf( but.getText() ) );

		but.setEnabled( false );

		setCalText();
	}


	// used to remove the last entry, if a operator or bracket just remove it ,
	// if a number then need to only remove last digit
	public void onBackClick( View v )
	{
		int lastArrayPos = inputs.size() - 1;

		if( lastArrayPos >= 0 )
		{
			String previousInput = checkRemainder( inputs.get( lastArrayPos ) );

			if( previousInput.equals( "" ) )
			{
				inputs.remove( lastArrayPos );
			}
			else if( isOperator( previousInput ) || isBracket( previousInput ) )
			{
				inputs.remove( lastArrayPos );
			}
			else
			{
				previousInput = previousInput.substring( 0, previousInput.length() - 1 );
				inputs.set( lastArrayPos, previousInput );

				if( previousInput.equals( "" ) )
				{
					inputs.remove( lastArrayPos );
				}

			}
		}

		if( inputs.size() == 0 )
		{
			inputs.add( "" );
		}

		setCalText();
	}


	// on any button click
	public void onButtonClick( View v )
	{
		Button but = (Button) findViewById( v.getId() );
		String input = but.getText().toString();
		int lastArrayPos = inputs.size() - 1;
		String previousInput = inputs.get( lastArrayPos );

		// if a number
		if( ( input.equals( "." ) ) || ( input.equals( "0" ) ) || ( input.equals( "1" ) ) || ( input.equals( "2" ) ) || ( input.equals( "3" ) )
				|| ( input.equals( "4" ) ) || ( input.equals( "5" ) ) || ( input.equals( "6" ) ) || ( input.equals( "7" ) ) || ( input.equals( "8" ) )
				|| ( input.equals( "9" ) ) )
		{
			// if in equation mode the replace the ? place holder with the value
			// entered, if ? then replace it else append on to the value at the
			// index where the ? was
			if( isEquationMode )
			{
				if( replaceIndex == -1 )
				{
					replaceIndex = inputs.indexOf( "?" );

					// if the index is still ? then the equation didnt initaly have a ? so update the last element
					if( replaceIndex == -1 )
					{
						inputs.set( lastArrayPos, previousInput + String.valueOf( but.getText() ) );
					}
					else
					{
						inputs.set( replaceIndex, String.valueOf( but.getText() ) );
					}
				}
				else
				{
					inputs.set( replaceIndex, inputs.get( replaceIndex ) + String.valueOf( but.getText() ) );
				}
			}
			else
			{
				if( isOperator( previousInput ) || isBracket( previousInput ) )
				{
					inputs.add( String.valueOf( but.getText() ) );
				}
				else
				{
					inputs.set( lastArrayPos, checkRemainder( previousInput ) + String.valueOf( but.getText() ) );
				}
			}

		}
		else if( isBracket( input ) ) // if a bracket
		{
			if( previousInput.equals( "" ) )
			{
				inputs.set( lastArrayPos, String.valueOf( but.getText() ) );
			}
			else
			{
				inputs.add( String.valueOf( but.getText() ) );
			}
		}
		else
		// if a operator
		{
			// if the previous input was an operator then replace it else add
			if( isOperator( previousInput ) || previousInput.equals( "" ) )
			{
				// inputs.set( inputs.size() - 1, checkRemainder(
				// String.valueOf( but.getText() ) ) );
				inputs.set( lastArrayPos, String.valueOf( but.getText() ) );
			}
			else
			{
				inputs.add( String.valueOf( but.getText() ) );
			}
		}

		setCalText();
	}


	// set the text of the equation
	private void setCalText()
	{
		et_cal.setText( "" );
		for(int i = 0; i < inputs.size(); i++)
		{
			et_cal.append( checkRemainder( inputs.get( i ) ) + " " );
		}
	}


	private Boolean isOperator( String operator )
	{
		return ( operator.equals( "+" ) ) || ( operator.equals( "-" ) ) || ( operator.equals( "*" ) ) || ( operator.equals( "/" ) );
	}


	private Boolean isBracket( String operator )
	{
		return ( operator.equals( "(" ) ) || ( operator.equals( ")" ) );
	}


	public void reset()
	{
		et_cal.setText( "" );
		inputs.clear();
		inputs.add( "" );
	}


	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.main, menu );
		return true;
	}


	@Override
	public boolean onPrepareOptionsMenu( Menu menu )
	{
		menu.getItem( 0 ).setEnabled( ( history.size() > 0 ) );

		return super.onPrepareOptionsMenu( menu );
	}


	private void clearHistory()
	{
		history.clear();
		invalidateOptionsMenu();
		reset();
	}


	private void setUpNewEquation()
	{
		reset();
		toggleEquationModeLayout( true );
		setIsEquationMode( false );
	}


	@Override
	public boolean onMenuItemSelected( int featureId, MenuItem item )
	{

		switch (item.getItemId())
		{

			case R.id.menu_item_clear_history:
				clearHistory();
				return true;

			case R.id.menu_item_new_equation:
				setUpNewEquation();
				return true;

			case R.id.menu_item_saved_equations:
				loadSavedEquation();
				return true;

			case R.id.menu_item_settings:
				openSettings();
				return true;

			case R.id.menu_item_undo:
				onUpClick();
				return true;

			default:
				return super.onMenuItemSelected( featureId, item );
		}

	}


	private void openSettings()
	{
		Intent i = new Intent( this, PreferencesActivity.class );
		startActivity( i );
	}


	private void loadSavedEquation()
	{
		toggleEquationModeLayout( false );

		DialogFragment savedEquationsDialog = new SavedEquationsDialog();
		savedEquationsDialog.show( getSupportFragmentManager(), "0" );
	}


	private void setEquation( String equation )
	{
		et_cal.setText( equation );

		setIsEquationMode( true );
		populateInputsArray();
	}


	// reconstruct the inputs ArrayList from the et_cal text, used by the up and
	// saved equations
	private void populateInputsArray()
	{
		inputs.clear();

		String[] currentCal = et_cal.getText().toString().split( " " );

		for(int i = 0; i < currentCal.length; i++)
		{
			inputs.add( currentCal[i] );
		}
	}


	// get the equation that was last calculated
	public void onUpClick()
	{
		et_cal.setText( history.get( history.size() - 1 ) );
		removeHistory();
		populateInputsArray();
	}


	// add to history,only invalidate action bar history going from 0 to 1
	private void addHistory( String newEquation )
	{
		history.add( newEquation );

		if( history.size() == 1 )
		{
			invalidateOptionsMenu();
		}
	}


	private void removeHistory()
	{
		history.remove( history.size() - 1 );

		if( history.size() == 0 )
		{
			invalidateOptionsMenu();
		}
	}


	private void toggleEquationModeLayout( Boolean isEquationEntryMode )
	{

		if( isEquationEntryMode )
		{
			btn_question.setVisibility( View.VISIBLE );
			btn_question.setEnabled( true );
			btn_equals.setText( "Save" );

			et_cal.setText( "" );
		}
		else
		{
			btn_question.setVisibility( View.INVISIBLE );
			btn_question.setEnabled( false );
			btn_equals.setText( "=" );
		}
	}


	private void setIsEquationMode( Boolean value )
	{
		isEquationMode = value;

		btn_plus.setEnabled( !isEquationMode );
		btn_mins.setEnabled( !isEquationMode );
		btn_mult.setEnabled( !isEquationMode );
		btn_divide.setEnabled( !isEquationMode );
		btn_leftBraket.setEnabled( !isEquationMode );
		btn_rightBraket.setEnabled( !isEquationMode );
	}


	@Override
	public void onDialogPositiveClick( DialogFragment dialog, String equation )
	{
		if( equation.equals( SavedEquationsDialog.NO_SAVED_EQUATIONS ) )
		{
			setUpNewEquation();
		}
		else
		{
			setEquation( equation );
		}
	}


	@Override
	public void onDialogNegativeClick( DialogFragment dialog, String equation )
	{
		if( !equation.equals( SavedEquationsDialog.NO_SAVED_EQUATIONS ) )
		{
			Toast toast = Toast.makeText( this, String.format( getString( R.string.toast_equation_deleted ), equation ), Toast.LENGTH_SHORT );
			toast.show();
		}
	}
}
