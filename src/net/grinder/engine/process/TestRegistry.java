// Copyright (C) 2001, 2002 Philip Aston
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

package net.grinder.engine.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.grinder.common.GrinderException;
import net.grinder.communication.RegisterTestsMessage;
import net.grinder.communication.Sender;
import net.grinder.engine.EngineException;
import net.grinder.plugininterface.GrinderPlugin;
import net.grinder.script.InvokeableTest;
import net.grinder.script.TestResult;
import net.grinder.statistics.TestStatisticsMap;


/**
 * @author Philip Aston
 * @version $Revision$
 */
public final class TestRegistry
{
    private static TestRegistry s_instance;

    private final Sender m_consoleSender;

    /**
     * A map of InvokeableTest to TestData's. (TestData is the class
     * this package uses to store information about InvokeableTests).
     * Synchronize on instance when accessesing.
     **/
    private final Map m_testMap = new TreeMap();

    private InvokeableTest[] m_tests;

    /**
     * A map of InvokeableTests to Statistics for passing elsewhere.
     **/
    private final TestStatisticsMap m_testStatisticsMap =
	new TestStatisticsMap();


    public static final TestRegistry getInstance()
    {
	return s_instance;
    }


    /**
     * Constructor.
     */

    TestRegistry(Sender consoleSender) throws EngineException
    {
	if (s_instance != null) {
	    throw new EngineException("Already initialised");
	}

	s_instance = this;

	m_consoleSender = consoleSender;
    }

    public RegisteredTest registerTest(GrinderPlugin plugin,
				       InvokeableTest test)
	throws GrinderException
    {
	final TestData newTestData;
	
	synchronized (this) {
	    final TestData existing = (TestData)m_testMap.get(test);

	    if (existing != null) {
		return existing;
	    }
	    else {
		newTestData = new TestData(plugin, test);
		m_testMap.put(test, newTestData);
		m_testStatisticsMap.put(test, newTestData.getStatistics());

		m_tests = null;
	    }
	}
	
	m_consoleSender.queue(
	    new RegisterTestsMessage(Collections.singleton(test)));

	return newTestData;
    }

    public TestResult invokeTest(RegisteredTest registeredTest)
	throws GrinderException
    {
	final TestData testData = (TestData)registeredTest;

	return ThreadContext.getThreadInstance().invokeTest(testData);
    }

    public final synchronized InvokeableTest[] getTests()
    {
	if (m_tests == null) {
	    m_tests =
		(InvokeableTest[])
		m_testMap.keySet().toArray(new InvokeableTest[0]);
	}

	return m_tests;
    }

    final TestStatisticsMap getTestStatisticsMap()
    {
	return m_testStatisticsMap;
    }

    public interface RegisteredTest
    {
    }
}
