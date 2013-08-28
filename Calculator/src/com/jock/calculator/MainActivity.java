package com.jock.calculator;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity
{
	private static final String APP_TAG = "MY_CAL";
	private static final String STATE_INPUTS = "inputs";
	private static final String STATE_HISTORY = "history";

	ArrayList<String> inputs = new ArrayList<String>();
	ArrayList<String> history = new ArrayList<String>();

	
	private EditText cal_et;
	private Button up_btn;
	private Boolean isEquationMode;
	private int replaceIndex;


	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		setContentView( R.layout.newcal );

		up_btn = (Button) findViewById( R.id.up_btn );
		cal_et = (EditText) findViewById( R.id.editText );

		// if there is a saved state restore it
		if( savedInstanceState != null )
		{
			inputs = savedInstanceState.getStringArrayList( STATE_INPUTS );
			history = savedInstanceState.getStringArrayList( STATE_HISTORY );

			setCalText();
			up_btn.setEnabled( ( history.size() > 0 ) );
		}
		else
		{
			inputs.add( "" );
			up_btn.setEnabled( false );
		}

		isEquationMode = false;
		replaceIndex = -1;
	}


	private ArrayList<String> remove( ArrayList<String> list, int from, int to )
	{
		for(int i = to; i > from; i--)
		{
			list.remove( i - 1 );
		}
		return list;
	}


	private ArrayList<String> bracketContents( ArrayList<String> inputStrings )
	{

		boolean cont = true;
		int openBracketPos = -1;

		while (cont)
		{
			int closeBracketPos = inputStrings.size();

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

				ArrayList<String> bracketContent = new ArrayList<String>( inputStrings.subList( openBracketPos, closeBracketPos ) );

				inputs = remove( inputs, openBracketPos, closeBracketPos );

				bracketContent.remove( 0 );

				if( bracketContent.get( bracketContent.size() - 1 ).equals( ")" ) )
				{
					bracketContent.remove( bracketContent.size() - 1 );
				}

				bracketContent = operatorPrecedence( bracketContent );
				bracketContent = calculate( bracketContent );

				inputs.add( openBracketPos, bracketContent.get( 0 ) );
			}
			else
			{

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
			if( equation.size() == 1 )
			{
				cont = false;
			}
			else
			{

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


	private ArrayList<String> operatorPrecedence( ArrayList<String> inputStrings )
	{
		Boolean cont = true;
		while (cont)
		{
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


	public void onEqualsClick( View v )
	{
		Boolean cont = true;

		history.add( cal_et.getText().toString() );
		up_btn.setEnabled( ( history.size() > 0 ) );

		checkForOrphanOp();

		inputs = bracketContents( inputs );

		inputs = operatorPrecedence( inputs );

		while (cont)
		{
			if( inputs.size() == 1 )
			{
				cont = false;
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
					/*
					 * else if( inputs.get( 1 ).equals( "*" ) ) { calculation =
					 * value1 * value2; } else if( inputs.get( 1 ).equals( "/" )
					 * ) { calculation = value1 / value2; }
					 */
					inputs.remove( 0 );
					inputs.remove( 0 );
					inputs.set( 0, String.valueOf( calculation ) );
				}
			}
		}

		cal_et.setText( checkRemainder( inputs.get( 0 ) ) );

		replaceIndex = -1;
		isEquationMode = false;
	}


	private void checkForOrphanOp()
	{
		if( isOperator( inputs.get( 0 ) ) )
		{
			inputs.add( 0, "0" );
		}
		else if( isOperator( inputs.get( inputs.size() - 1 ) ) )
		{
			if( inputs.get( inputs.size() - 1 ).equals( "/" ) )
			{
				inputs.add( inputs.size(), "1" );
			}
			else
			{
				inputs.add( inputs.size(), "0" );
			}
		}
	}


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


	public void onButtonClick( View v )
	{
		Button but = (Button) findViewById( v.getId() );
		String input = but.getText().toString();
		String previousInput = inputs.get( inputs.size() - 1 );

		if( ( input.equals( "." ) ) || ( input.equals( "0" ) ) || ( input.equals( "1" ) ) || ( input.equals( "2" ) ) || ( input.equals( "3" ) )
				|| ( input.equals( "4" ) ) || ( input.equals( "5" ) ) || ( input.equals( "6" ) ) || ( input.equals( "7" ) ) || ( input.equals( "8" ) )
				|| ( input.equals( "9" ) ) )
		{

			if( isEquationMode )
			{
				if( replaceIndex == -1 )
				{
					replaceIndex = inputs.indexOf( "?" );
					inputs.set( replaceIndex, String.valueOf( but.getText() ) );
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
					inputs.set( inputs.size() - 1, checkRemainder( previousInput ) + String.valueOf( but.getText() ) );
				}
			}

		}
		else if( isBracket( input ) )
		{
			if( previousInput.equals( "" ) )
			{
				inputs.set( inputs.size() - 1, String.valueOf( but.getText() ) );
			}
			else
			{
				inputs.add( String.valueOf( but.getText() ) );
			}
		}
		else
		{
			if( isOperator( previousInput ) || inputs.get( inputs.size() - 1 ).equals( "" ) )
			{
				// inputs.set( inputs.size() - 1, checkRemainder(
				// String.valueOf( but.getText() ) ) );
				inputs.set( inputs.size() - 1, String.valueOf( but.getText() ) );
			}
			else
			{
				inputs.add( String.valueOf( but.getText() ) );
			}
		}

		setCalText();
	}


	private void setCalText()
	{
		cal_et.setText( "" );
		for(int i = 0; i < inputs.size(); i++)
		{
			cal_et.append( checkRemainder( inputs.get( i ) ) + " " );
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


	public void onClearClick( View v )
	{
		reset();
	}


	private void reset()
	{
		cal_et.setText( "" );
		inputs.clear();
		inputs.add( "" );

		up_btn.setEnabled( ( history.size() > 0 ) );

	}


	@SuppressWarnings("unused")
	private void logMe( String logThis )
	{
		Log.d( APP_TAG, logThis );
	}


	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.main, menu );
		return true;
	}


	private void clearHistory()
	{
		history.clear();
		reset();
	}


	@Override
	public boolean onMenuItemSelected( int featureId, MenuItem item )
	{

		switch (item.getItemId())
		{
		case R.id.action_clear_history:
			clearHistory();
			return true;

		case R.id.xxx:
			setEquation( "20 + 5 + ( ? * 3 )" );
			return true;

		default:
			return super.onMenuItemSelected( featureId, item );
		}

	}


	private void setEquation( String equation )
	{
		cal_et.setText( equation );

		setIsEquationMode( true );
		populateInputsArray();
	}


	private void setIsEquationMode( Boolean value )
	{
		isEquationMode = value;

		// disable operator buttons

	}


	private void populateInputsArray()
	{
		inputs.clear();

		String[] currentCal = cal_et.getText().toString().split( " " );

		for(int i = 0; i < currentCal.length; i++)
		{
			inputs.add( currentCal[i] );
		}
	}


	@Override
	protected void onSaveInstanceState( Bundle outState )
	{
		outState.putStringArrayList( STATE_INPUTS, inputs );
		outState.putStringArrayList( STATE_HISTORY, history );

		super.onSaveInstanceState( outState );
	}


	public void onUpClick( View v )
	{
		cal_et.setText( history.get( history.size() - 1 ) );

		history.remove( history.size() - 1 );

		populateInputsArray();

		up_btn.setEnabled( ( history.size() > 0 ) );
	}

}
