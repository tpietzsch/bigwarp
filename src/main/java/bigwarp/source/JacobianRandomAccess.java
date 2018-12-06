package bigwarp.source;


import java.util.Arrays;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import net.imglib2.AbstractLocalizable;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.realtransform.AffineTransform;
import net.imglib2.realtransform.inverse.DifferentiableRealTransform;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.GenericComposite;

public class JacobianRandomAccess< T extends RealType<T>> extends AbstractLocalizable implements RandomAccess< T >
{

	final DifferentiableRealTransform xfm;
	final T value;

	final int numSpatialDimensions;
	final int tensorDimension;
	final AffineTransform currentJacobian;
	boolean positionChanged = true;

	final double[] realPos;
	
	public JacobianRandomAccess( long[] dimensions )
	{
		// should assume identity transformation
		this( dimensions, null, null );
	}
	
	public JacobianRandomAccess( long[] dimensions, T value, DifferentiableRealTransform xfm )
	{
		this( dimensions.length, value, xfm );
	}

	public JacobianRandomAccess( int nsd, T value, DifferentiableRealTransform xfm )
	{
		super( nsd + 1 );
		this.numSpatialDimensions = nsd;

		// the non-spatial dimension in which the jacobian values can be accessed
		tensorDimension = nsd; 

		this.value = value;
		this.xfm = xfm;
		currentJacobian = new AffineTransform( nsd );
		realPos = new double[ nsd ];
	}
	
	public static <T extends RealType<T>> JacobianRandomAccessible<T> create( 
			int nsd, T type, DifferentiableRealTransform transform )
	{
		return new JacobianRandomAccessible<T>( 
				new JacobianRandomAccess<T>( nsd, type, transform ));
	}

	public static <T extends RealType<T>> RandomAccessibleInterval<T> create( 
			Interval spatialInterval, T type, DifferentiableRealTransform transform )
	{
		int nsd = spatialInterval.numDimensions();
			
		long[] min = new long[ nsd + 1 ];
		long[] max = new long[ nsd + 1 ];
		
		int ntd = nsd * nsd; // number of tensor elements
		for( int d = 0; d < nsd + 1; d++ )
		{
			if( d < nsd )
			{
				min[ d ] = spatialInterval.min( d );
				max[ d ] = spatialInterval.max( d );
			}
			else
			{
				min[ d ] = 0;
				max[ d ] = ntd - 1;
			}
		}

		Interval interval = new FinalInterval( min, max );
		
		System.out.println( "total jac interval min: " + Arrays.toString( min ));
		System.out.println( "total jac interval max: " + Arrays.toString( max ));

		JacobianRandomAccessible<T> raj = new JacobianRandomAccessible<T>( 
				new JacobianRandomAccess<T>( nsd, type, transform ));
	
		return Views.interval( raj, interval );
	}
	
	public static <T extends RealType<T>> RandomAccessibleInterval<T> createJacobianDeterminant(
			Interval spatialInterval, T type, DifferentiableRealTransform transform )
	{
		int n = spatialInterval.numDimensions();
		DenseMatrix64F mtx = new DenseMatrix64F( n, n );

		RandomAccessibleInterval<T> jacimg = create( spatialInterval, type, transform );
		RandomAccessibleInterval<? extends GenericComposite<T>> collapsed = Views.collapse( jacimg );

		return Converters.convert( collapsed, 
				new Converter< GenericComposite< T >, T >()
				{
					@Override
					public void convert(GenericComposite<T> input, T output)
					{
						int k = 0;
						for( int j = 0; j < n; j++ )
							for( int i = 0; i < n; i++ )
								mtx.set( i, j, input.get( k ).getRealDouble());
	
						output.setReal( CommonOps.det( mtx ));
					}
				},
				type.copy());
	}

	/**
	 * 
	 * @return true if the position changed
	 */
	public boolean changedPosition()
	{
		return positionChanged;
	}

	@Override
	public T get() 
	{
		if( positionChanged  && xfm != null ) 
		{
			// recompute jacobian
			localizeSpatial( realPos );
			AffineTransform newJacobian = xfm.jacobian( realPos );
			currentJacobian.set( newJacobian );
		}
		
		int tensorP = getIntPosition( tensorDimension );
		int column = tensorP % numSpatialDimensions;
		int row = ( tensorP - column ) / numSpatialDimensions;

		value.setReal( currentJacobian.get( row, column ) );
		return value;
	}
	
	public void localizeSpatial( double[] p )
	{
		for( int d = 0; d < numSpatialDimensions; d++ )
			p[ d ] = getDoublePosition( d );
	}
	
	public RandomAccess<T> copy() 
	{
		return new JacobianRandomAccess< T >( new long[ position.length ], value.copy(), xfm );
	}

	public RandomAccess<T> copyRandomAccess() 
	{
		return copy();
	}

	@Override
	public void fwd( final int d )
	{
		++position[ d ];
		positionChanged = ( d != tensorDimension );
	}

	@Override
	public void bck( final int d )
	{
		--position[ d ];
		positionChanged = ( d != tensorDimension );
	}

	@Override
	public void move( final int distance, final int d )
	{
		position[ d ] += distance;
		positionChanged = ( d != tensorDimension );
	}

	@Override
	public void move( final long distance, final int d )
	{
		position[ d ] += distance;
		positionChanged = ( d != tensorDimension );
	}

	@Override
	public void move( final Localizable localizable )
	{
		for ( int d = 0; d < n; ++d )
		{
			final int distance = localizable.getIntPosition( d );
			position[ d ] += distance;
			
			if( d != tensorDimension && distance != 0 )
				positionChanged = true;
		}
	}

	@Override
	public void move( final int[] distance )
	{
		for ( int d = 0; d < n; ++d )
		{
			position[ d ] += distance[ d ];

			if( d != tensorDimension && distance[ d ] != 0 )
				positionChanged = true;
		}
	}

	@Override
	public void move( final long[] distance )
	{
		for ( int d = 0; d < n; ++d )
		{
			position[ d ] += distance[ d ];

			if( d != tensorDimension && distance[ d ] != 0 )
				positionChanged = true;
		}
	}

	@Override
	public void setPosition( final Localizable localizable )
	{
		for ( int d = 0; d < n; ++d )
		{
			if( d != tensorDimension && localizable.getIntPosition( d ) != position[ d ] )
				positionChanged = true;

			position[ d ] = localizable.getIntPosition( d );
		}
	}

	@Override
	public void setPosition( final int[] pos )
	{
		for ( int d = 0; d < n; ++d )
		{
			if( d != tensorDimension && pos[ d ] != position[ d ] )
				positionChanged = true;

			position[ d ] = pos[ d ];
		}
	}

	@Override
	public void setPosition( final long[] pos )
	{
		for ( int d = 0; d < n; ++d )
		{
			if( d != tensorDimension && pos[ d ] != position[ d ] )
				positionChanged = true;

			position[ d ] = pos[ d ];
		}
	}

	@Override
	public void setPosition( final int pos, final int d )
	{
		position[ d ] = pos;
		positionChanged = ( d != tensorDimension );
	}

	@Override
	public void setPosition( final long pos, final int d )
	{
		position[ d ] = ( int ) pos;
		positionChanged = ( d != tensorDimension );
	}
	
	public static class JacobianRandomAccessible<T extends RealType<T>> implements RandomAccessible<T>
	{
		final private JacobianRandomAccess<T> access;

		public JacobianRandomAccessible( final JacobianRandomAccess<T> access )
		{
			this.access = access;
		}

		@Override
		public int numDimensions()
		{
			return access.numDimensions();
		}

		@Override
		public RandomAccess<T> randomAccess() {
			return access;
		}

		@Override
		public RandomAccess<T> randomAccess(Interval interval)
		{
			return access;
		}
		
	}
		

}
