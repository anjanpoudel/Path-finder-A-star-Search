import java.util.ArrayList;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;
import java.util.Comparator;
import java.util.TreeSet;
import java.lang.Exception;
import java.lang.Math;
import java.util.LinkedList;
import java.util.Collections;

class Agent
 {
	 private GameState init_state;
	 private GameState final_state;
   private ArrayList<GameState> list = new ArrayList<GameState>();
   private float final_x;
   private float final_y;
   private SearchAlgo search = new SearchAlgo();
   private GameState goal_state;
   TreeSet<GameState> front = new TreeSet<GameState>();
   private int mouse_click;

	void drawPlan(Graphics g, Model m)
	{
		g.setColor(Color.red);
		//g.drawLine((int)m.getX(), (int)m.getY(), (int)m.getDestinationX(), (int)m.getDestinationY());
    ////System.out.println("Drawing path");
    if(list.isEmpty())
    {
      return;
    }

    for(GameState state : list)
    {
      g.drawLine((int)state.get_parent().get_x(), (int)state.get_parent().get_y(), (int)state.get_x(), (int)state.get_y());
    }
	}

  void draw_frontere(Graphics g)
  {
    g.setColor(Color.green);
    //TreeSet<GameState> front = (TreeSet<GameState>)search.get_frontire();
    //System.out.println("Drawing frontire");
    if(front == null)
    {
      return;
    }

    for(GameState state: front)
    {
      g.fillOval((int)state.get_x(), (int)state.get_y(), 10, 10);
    }

    front.clear();

  }



	void update(Model m)
	{
    if(final_x >= 0 && final_y >= 0)
    {
      m.setDestination(final_x, final_y);
    }

		Controller c = m.getController();
		while(true)
		{
			MouseEvent e = c.nextMouseEvent();
			if(e == null)
				break;

        final_x = e.getX();
        final_x = Math.round(final_x/10)*10;
        final_y = e.getY();
        final_y = Math.round(final_y/10)*10;
        mouse_click = e.getButton();

			m.setDestination(final_x,final_y);

    }
      int xx = Math.round(m.getX()/10)*10;
      int yy = Math.round(m.getY()/10)*10;

      int dist_x = Math.round(m.getDestinationX()/10)*10;
      int dist_y = Math.round(m.getDestinationY()/10)*10;

      if(xx != dist_x || yy != dist_y)
      {
        init_state = new GameState(xx,yy);
  			final_state = new GameState(dist_x, dist_y);

        list.clear();

        if(mouse_click == 1)
        {
          SearchAlgo ucs = new SearchAlgo();
          list = ucs.uniform_cost_search (init_state, final_state, m);
          this.front = ucs.get_frontire();
        }
        else if (mouse_click == 3)
        {
          SearchAlgo astar = new SearchAlgo();
          list = astar.a_star_search(init_state, final_state, m);
          this.front = astar.get_frontire();
        }

        if(list.size() - 1 >= 0)
        {
          m.setDestination(list.get(list.size() - 1).get_x(), list.get(list.size() - 1).get_y());
        }
        else
        {
          m.setDestination(final_x, final_y);
          list.clear();
        }
        Collections.reverse(list);
      }
	}


	public static void main(String[] args) throws Exception
	{
		Controller.playGame();

	}
}

class SearchAlgo
{

  private TreeSet<GameState> queue;
  private TreeSet<GameState> set;
  private TreeSet<GameState> frontier;
  private StateComparator s_comp = new StateComparator();
  private CostComparator c_comp = new CostComparator();
  private HComparator h_comp = new HComparator();

  private ArrayList<GameState> optimal_path = new ArrayList<GameState>();
  private GameState wining_state;

	ArrayList<GameState> uniform_cost_search(GameState init_state, GameState final_state, Model m)
	{
		////System.out.println("Calling Uniform cost search ");

		////System.out.println("-----------------------------------------------------------\n\n\n");

		set = new TreeSet<GameState>(s_comp);
		queue = new TreeSet<GameState>(c_comp);

		//GameState goal_state = null;

		GameState root = init_state;
		root.set_cost(0.0f);
		root.set_parent(null);

		queue.add(root);
		////System.out.println("\npush to queue x = "  + root.get_x() + " y = " + root.get_y() + " cost = " + root.get_cost());
		set.add(root);
		////System.out.println("push to set x = "  + root.get_x() + " y = " + root.get_y() + " cost = " + root.get_cost());

		while(! queue.isEmpty())
		{
			GameState s = (GameState)queue.pollFirst();
			////System.out.println("\npop x = "  + s.get_x() + " y = " + s.get_y() + " cost = " + s.get_cost());

			if((s.get_x() == Math.floor(final_state.get_x())) && (s.get_y() == Math.floor(final_state.get_y())))
			{
				////System.out.println("Goal Found");
        frontier = queue;
        return get_path(s);
			}

			for(int i=0; i < 8; i++)
			{
				int x =0;
				int y =0;

				GameState child = new GameState(s);
				// move right
				if(i == 0)       { x = 10; y = 0; }
				//move left
				else if(i ==1)   { x = -10; y =0; }
				//move up
				else if (i == 2) { x = 0; y = -10;}
				//move down
				else if (i == 3) { x = 0; y = 10; }

				// move up and right
				else if (i == 4) { x = 10; y = -10; }
				//move down and right
				else if (i ==5)  { x = 10; y = 10; }
				//move up and left
				else if (i ==6)  { x= -10; y = -10;}
				//move down and left
				else if (i == 7) {x = -10; y = 10;}


				child.set_x(child.get_x() + x);
				child.set_y(child.get_y() + y);

        if(is_valid((int)child.get_x() , (int)child.get_y(), m) == false )
        {
          break;
        }

        float cost = calculate_cost(child,s,m);
				//float cost = (1/(m.getTravelSpeed(child.get_x(), child.get_y())));
				////System.out.println("child " + i + " is x = " + child.get_x() + " y = " + child.get_y() + "cost = " + child.get_cost());

				if(set.contains(child))
				{
					// Add it to a set
					GameState old_child = (GameState) set.floor(child);
					// //System.out.println("\nOld child x = " + old_child.get_x() + " y = " + old_child.get_y());
					// //System.out.println("child x = " + child.get_x() + " y = " + child.get_y());
					if(!set.contains(old_child))
					{
						throw new RuntimeException("Error, child is not in the set, line 219");
					}

					if( (old_child.get_x() != child.get_x()) && (old_child.get_y() != child.get_y()))
					{
						// //System.out.println("child x = " + child.get_x() + " y = " + child.get_y());
						// //System.out.println("Old child x = " + old_child.get_x() + " y = " + old_child.get_y());

						throw new RuntimeException("Child and old child are not equal");
					}

					if(s.get_cost() + cost < old_child.get_cost())
					{
						old_child.set_cost(s.get_cost() + cost );
						old_child.set_parent(s);
					}
				}
				else
				{
					child.set_cost(s.get_cost() + cost);
					child.set_parent(s);
					queue.add(child);
					set.add(child);
					////System.out.println("push to queue x = "  + child.get_x() + " y = " + child.get_y() + " cost = " + child.get_cost());
					////System.out.println("push to set x = "  + child.get_x() + " y = " + child.get_y() + " cost = " + child.get_cost());
				}

			}

		}
		throw new RuntimeException("There is no path to the goal ");

		// --------------- UCS End ---------------------------
	}

  ArrayList<GameState> a_star_search(GameState init_state, GameState final_state, Model m )
  {
    set = new TreeSet<GameState>(s_comp);
		queue = new TreeSet<GameState>(h_comp);

    GameState root = init_state;
		// root.set_cost(0.0f);
		// root.set_parent(null);
    float max_speed = 0.2f;

		queue.add(root);
		//System.out.println("\npush to queue x = "  + root.get_x() + " y = " + root.get_y() + " heu = " + root.get_h());
		set.add(root);
		////System.out.println("push to set x = "  + root.get_x() + " y = " + root.get_y() + " cost = " + root.get_cost());

		while(! queue.isEmpty())
		{
			GameState s = (GameState)queue.pollFirst();
			//System.out.println("\npop x = "  + s.get_x() + " y = " + s.get_y() + " heu = " + s.get_h());

			if((s.get_x() == Math.floor(final_state.get_x())) && (s.get_y() == Math.floor(final_state.get_y())))
			{
				////System.out.println("Goal Found");
        frontier = queue;
        return this.get_path(s);
			}

			for(int i=0; i < 8; i++)
			{
				int x =0;
				int y =0;

				GameState child = new GameState(s);
				// move right
				if(i == 0)       { x = 10; y = 0; }
				//move left
				else if(i ==1)   { x = -10; y =0; }
				//move up
				else if (i == 2) { x = 0; y = -10;}
				//move down
				else if (i == 3) { x = 0; y = 10; }

				// move up and right
				else if (i == 4) { x = 10; y = -10; }
				//move down and right
				else if (i ==5)  { x = 10; y = 10; }
				//move up and left
				else if (i ==6)  { x= -10; y = -10;}
				//move down and left
				else if (i == 7) {x = -10; y = 10;}


				child.set_x(child.get_x() + x);
				child.set_y(child.get_y() + y);

        if(is_valid((int)child.get_x() , (int)child.get_y(), m) == false )
        {
          break;
        }
        //
        // float dx = child.get_x() - m.getDestinationX();
        // float dy = child.get_y()  - m.getDestinationY();
        //
        // float distance = (float)Math.sqrt(dx*dx + dy*dy);
				// float heuristic = (distance/max_speed);

				if(set.contains(child))
				{
					// Add it to a set
					GameState old_child = (GameState) set.floor(child);
					// //System.out.println("\nOld child x = " + old_child.get_x() + " y = " + old_child.get_y());
					// //System.out.println("child x = " + child.get_x() + " y = " + child.get_y());
					if(!set.contains(old_child))
					{
						throw new RuntimeException("Error, child is not in the set, line 319");
					}

					if( (old_child.get_x() != child.get_x()) && (old_child.get_y() != child.get_y()))
					{
						// //System.out.println("child x = " + child.get_x() + " y = " + child.get_y());
						// //System.out.println("Old child x = " + old_child.get_x() + " y = " + old_child.get_y());

						throw new RuntimeException("Child and old child are not equal");
					}

					if(s.get_cost() + calculate_cost(child, s, m)   < old_child.get_cost())
					{
						old_child.set_cost(s.get_cost() +  calculate_cost(child, s, m));
						old_child.set_parent(s);
					}
				}
				else
				{
					child.set_h(get_heuristic(child,max_speed, m));
          child.set_cost(s.get_cost() +  calculate_cost(child, s, m));
          //child.set_cost(s.get_cost());
					child.set_parent(s);
					queue.add(child);
					set.add(child);
					//System.out.println("push to queue x = "  + child.get_x() + " y = " + child.get_y() + " hue = " + child.get_h());
					//System.out.println("push to set x = "  + child.get_x() + " y = " + child.get_y() + " heu = " + child.get_h());
				}

			}

		}
		throw new RuntimeException("There is no path to the goal ");

    //----Astar ends here
  }

  float get_heuristic(GameState init, float speed, Model m)
  {
    float heu = 0.0f;
		float dx = init.get_x() - m.getDestinationX();
		float dy = init.get_x() - m.getDestinationY();
		float distance = (float) Math.sqrt(dx*dx + dy*dy);
		  heu = (distance / speed)/10;
		return heu;
  }

  float calculate_cost(GameState init, GameState finaal, Model m)
  {
    float cost = 0;
  //System.out.println("Loc: " + x + ", " + y);
    float speed = m.getTravelSpeed(init.get_x(), init.get_y());
    float dx = init.get_x() - finaal.get_x();
    float dy = init.get_y() - finaal.get_y();

    float distance = (float) Math.sqrt(dx*dx + dy*dy);
    cost = distance / speed;
    return cost;
  }

  TreeSet<GameState> get_frontire()
  {
    return frontier;
  }

  ArrayList<GameState> get_path(GameState child)
  {
    optimal_path.clear();
    GameState parent = child.get_parent();

    while(parent != null )
    {
      optimal_path.add(child);
      child = parent;
      parent = child.get_parent();
    }
    return optimal_path;
  }

  boolean is_valid(int x, int y, Model m)
  {
    if(x > m.XMAX)
      return false;

    if( x < 0 )
      return false;

    if(y > m.YMAX)
      return false;

    if(y < 0)
      return false;

    return true;
  }
}


class GameState
{
	private float x;
	private float y;
	private float cost;
	private GameState parent;
  private float heuristic = 0.0f;

	GameState(float x, float y)
	{
		this.x = x;
		this.y = y;
		this.cost = 0.0f;
		this.parent = null;
	}

	GameState(GameState that)
	{
		this.x = that.x;
		this.y = that.y;
		this.cost = that.cost;
		this.parent = that;
    this.heuristic = 0.0f;
	}

	void set_x(float x)
	{
		this.x = x;
	}
	void set_y(float y)
	{
		this.y = y;
	}
	void set_cost(float cost)
	{
		this.cost = cost;
	}

	void set_parent(GameState parent)
	{
		this.parent = parent;
	}
  void set_h(float h)
  {
    this.heuristic = h ;
  }

	float get_x()
	{
		return this.x;
	}
	float get_y()
	{
		return this.y;
	}
	float get_cost()
	{
		return this.cost;
	}

  float get_h()
  {
    return this.heuristic;
  }

	GameState get_parent()
	{
		return parent;
	}

}


class StateComparator implements Comparator<GameState>
{
	public int compare(GameState a1, GameState b1)
	{
		if(a1.get_x() > b1.get_x())
		{
			return 1;
		}
    else if(a1.get_x() < b1.get_x())
    {
      return -1;
    }
    else if(a1.get_y() > b1.get_y())
    {
      return 1;
    }
    else if(a1.get_y() < b1.get_y())
    {
      return -1;
    }


		return 0;
	}
}

class CostComparator implements Comparator<GameState>
{
	public int compare(GameState a, GameState b)
	{
		if(a.get_cost() > b.get_cost())
		{
			return 1;
		}
		else
		{
			return -1;
		}
	}
}

class HComparator implements Comparator<GameState>
{
	public int compare(GameState a, GameState b)
	{
		if((a.get_cost() + a.get_h()) > (b.get_cost() + b.get_h()))
		{
			return 1;
		}
		else if ((a.get_cost() + a.get_h()) < (b.get_cost() + b.get_h()))
		{
			return -1;
		}
    else if(a.get_x() > b.get_x())
		{
			return 1;
		}
    else if(a.get_x() < b.get_x())
    {
      return -1;
    }
    else if(a.get_y() > b.get_y())
    {
      return 1;
    }
    else if(a.get_y() < b.get_y())
    {
      return -1;
    }

    return 0;
	}

}
