// Copyright (C) 2000 Paco Gomez
// Copyright (C) 2000, 2001, 2002 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.statistics;

import junit.framework.TestCase;
import junit.swingui.TestRunner;
//import junit.textui.TestRunner;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.grinder.common.GrinderException;
import net.grinder.common.StubTest;
import net.grinder.common.Test;


/**
 * Unit test case for <code>StatisticsTable</code>.
 *
 * @author Philip Aston
 * @version $Revision$
 * @see RawStatistics
 */
public class TestStatisticsTable extends TestCase
{
    public static void main(String[] args)
    {
	TestRunner.run(TestStatisticsTable.class);
    }

    public TestStatisticsTable(String name)
    {
	super(name);
    }

    private TestStatisticsMap m_testStatisticsMap;

    private StatisticsView m_statisticsView;

    protected void setUp() throws Exception
    {
	m_testStatisticsMap = new TestStatisticsMap();

	final StatisticsIndexMap indexMap = StatisticsIndexMap.getInstance();

	final StatisticsIndexMap.LongIndex aIndex =
	    indexMap.getIndexForLong("userLong0");
	final StatisticsIndexMap.LongIndex bIndex =
	    indexMap.getIndexForLong("userLong1");

	final ExpressionView[] expressionViews = {
	    new ExpressionView("A", "", "userLong0"),
	    new ExpressionView("B", "", "userLong1"),
	    new ExpressionView("A plus B", "", "(+ userLong0 userLong1)"),
	    new ExpressionView("A divided by B", "",
			       "(/ userLong0 userLong1)"),
	};

	m_statisticsView = new StatisticsView();

	for (int i=0; i<expressionViews.length; ++i) {
	    m_statisticsView.add(expressionViews[i]);
	}

	final Test[] tests = {
	    new StubTest(9, "Test 9"),
	    new StubTest(3, "Test 3"),
	    new StubTest(113, "Another test"),
	};

	final RawStatistics[] rawStatistics = new RawStatistics[tests.length];

	final TestStatisticsFactory factory =
	    TestStatisticsFactory.getInstance();

	for (int i=0; i<tests.length; ++i) {
	    rawStatistics[i] = new RawStatisticsImplementation();
	    rawStatistics[i].addValue(aIndex, i);
	    rawStatistics[i].addValue(bIndex, i+1);

	    final TestStatistics testStatistics = factory.create();
	    testStatistics.add(rawStatistics[i]);

	    m_testStatisticsMap.put(tests[i], testStatistics);
	}
    }

    public void testStatisticsTable() throws Exception
    {
	final StringWriter expected = new StringWriter();
	final PrintWriter in = new PrintWriter(expected);
	in.println("             A            B            A plus B     A divided by ");
	in.println("                                                    B            ");
	in.println();
	in.println("Test 3       1            2            3            0.50          \"Test 3\"");
	in.println("Test 9       0            1            1            0.00          \"Test 9\"");
	in.println("Test 113     2            3            5            0.67          \"Another test\"");
	in.println();
	in.println("Totals       3            6            9            0.50         ");
	in.close();

	final StatisticsTable table = new StatisticsTable(m_statisticsView,
							  m_testStatisticsMap);

	final StringWriter stringWriter = new StringWriter();
	final PrintWriter out = new PrintWriter(stringWriter);
	table.print(out);
	out.close();

	assertEquals(expected.getBuffer().toString(),
		     stringWriter.getBuffer().toString());
    }
}
